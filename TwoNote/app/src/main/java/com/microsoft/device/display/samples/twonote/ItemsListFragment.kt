/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.microsoft.device.display.samples.twonote.model.DataProvider
import com.microsoft.device.display.samples.twonote.model.DirEntry
import com.microsoft.device.display.samples.twonote.model.Inode
import com.microsoft.device.display.samples.twonote.model.Note
import com.microsoft.device.dualscreen.layout.ScreenHelper
import java.io.*
import java.time.LocalDateTime
import kotlin.collections.ArrayList

class ItemsListFragment : Fragment(), AdapterView.OnItemClickListener {
    private var arrayAdapter: ArrayAdapter<Inode>? = null
    private var listView: ListView? = null
    private lateinit var inodes: ArrayList<Inode>
    private var selectedItemPosition: Int = 0
    private val ROOT = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inodes = DataProvider.notes
        activity?.let {
            arrayAdapter = ArrayAdapter(
                it,
                android.R.layout.simple_list_item_activated_1,
                inodes
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_items_list, container, false)
        // writeDirEntry("", DirEntry(mutableListOf()))  // uncomment this to clear record of all root entries (use for testing)

        listView = view.findViewById(R.id.list_view)
        listView?.let {
            it.adapter = arrayAdapter
            it.choiceMode = ListView.CHOICE_MODE_SINGLE
            it.onItemClickListener = this
        }

        view.findViewById<FloatingActionButton>(R.id.add_fab).setOnClickListener {
            // Set selected item to newly created note (first element in list)
            startNoteFragment(addInode(ROOT))
        }

        loadDirectory(ROOT)

        return view
    }

    private fun setSelectedItem(position: Int) {
        listView?.setItemChecked(position, true)
        selectedItemPosition = position
    }

    override fun onItemClick(adapterView: AdapterView<*>, item: View, position: Int, rowId: Long) {
        startNoteFragment(position)
    }

    private fun startNoteFragment(position: Int) {
        setSelectedItem(position)

        arrayAdapter?.getItem(position)?.let { inode ->
            activity?.let { activity ->
                var note = loadNote("", "/n" + inode.id)
                if (note == null)
                    note = Note(inode.id)

                if (ScreenHelper.isDualMode(activity)) {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.dual_screen_end_container_id,
                            NoteFragment.newInstance(note), null
                        ).commit()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.single_screen_container_id,
                            NoteFragment.newInstance(note), null
                        ).addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    fun updateNote(title: String) {
        var inode: Inode?

        arrayAdapter?.let { array ->
            inode = array.getItem(selectedItemPosition)

            inode?.let {
                DataProvider.notes.remove(it)
                it.title = title
                it.dateModified = LocalDateTime.now()
                DataProvider.notes.add(selectedItemPosition, it)
            }
        }
    }

    /**
     * Sort list of notes based on date modified (most recently edited at top)
     *
     * REVISIT: when should it be sorted? (ex: in dual-screen view, not done editing,
     * should it be at original position or pop to top as soon as editing begins?)
     * Should also adjust the listView's "selectedItemPosition" highlight feature (and the
     * index of the attribute "selectedItemPosition")
     */
    private fun sortArray() {
        arrayAdapter?.sort { one, two -> two.dateModified.compareTo(one.dateModified) }
    }

    // loads inode information from the current directory into the DataProvider
    private fun loadDirectory(subDir: String) {
        if (DataProvider.notes.isEmpty()) {
            readDirEntry(subDir)?.let { notes ->
                for (inode in notes.inodes) {
                    DataProvider.addNote(inode)
                }
            }
        }
    }

    // add a new inode
    private fun addInode(subDir: String): Int {
        val inode = Inode("Note 0", LocalDateTime.now(), 0)
        readDirEntry(subDir)?.let { entry ->
            if (entry.inodes.isNotEmpty()) {
                inode.id = entry.inodes[entry.inodes.lastIndex].id + 1
                inode.title = "Note " + inode.id
                entry.inodes.add(Inode(inode.title, inode.dateModified, inode.id))
                writeDirEntry(subDir, entry)
                DataProvider.addNote(inode)
                return entry.inodes.lastIndex
            } else {
                return createNewInode(subDir, inode, entry)
            }
        }
        val newEntry = DirEntry(mutableListOf())
        return createNewInode(subDir, inode, newEntry)
    }

    // helper for addInode, creates a new Inode entry
    private fun createNewInode(subDir: String, inode: Inode, dirEntry: DirEntry): Int {
        dirEntry.inodes.add(Inode(inode.title, inode.dateModified, inode.id))
        writeDirEntry(subDir, dirEntry)
        DataProvider.addNote(inode)
        return dirEntry.inodes.lastIndex
    }

    // reads a file and parses note data
    private fun loadNote(subDir: String, noteName: String): Note? {
        val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
        val file = File(path + subDir + noteName)
        var fileStream: FileInputStream? = null
        var objectStream: ObjectInputStream? = null

        try {
            fileStream = FileInputStream(file)
            objectStream = ObjectInputStream(fileStream)
            val note = objectStream.readObject()
            if (note is Note) {
                return note
            } else {
                Log.e(this.javaClass.toString(), "Error: loaded file is not of type Note")
                return null
            }
        } catch (e: FileNotFoundException) {
            Log.e(this.javaClass.toString(), e.message.toString())
            return null
        } catch (e: InvalidClassException) {
            Log.e(this.javaClass.toString(), e.message.toString())
            return null
        } finally {
            objectStream?.close()
            fileStream?.close()
        }
    }

    // reads directory entry to get inodes
    private fun readDirEntry(subDir: String): DirEntry? {
        val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
        val file = File(path + subDir + "/dEntry")
        var fileStream: FileInputStream? = null
        var objectStream: ObjectInputStream? = null

        try {
            fileStream = FileInputStream(file)
            objectStream = ObjectInputStream(fileStream)
            val entry = objectStream.readObject()
            if (entry is DirEntry) {
                return entry
            } else {
                Log.e(this.javaClass.toString(), "Error: loaded file is not of type DirEntry")
                return null
            }
        } catch (e: FileNotFoundException) {
            val entry = DirEntry(mutableListOf())
            writeDirEntry(subDir, entry) // create a new dir entry
            return entry
        } catch (e: InvalidClassException) {
            val entry = DirEntry(mutableListOf())
            writeDirEntry(subDir, entry)
            return entry
        } finally {
            objectStream?.close()
            fileStream?.close()
        }
    }

    // update/create directory entry
    fun writeDirEntry(subDir: String, entry: DirEntry) {
        val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
        val file = File(path + subDir + "/dEntry")
        val fileStream = FileOutputStream(file)
        val objectStream = ObjectOutputStream(fileStream)
        objectStream.writeObject(entry)
        objectStream.close()
        fileStream.close()
    }
}
