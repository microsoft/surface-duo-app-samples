/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.os.Bundle
import android.provider.ContactsContract
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
import com.microsoft.device.display.samples.twonote.model.Note
import com.microsoft.device.dualscreen.layout.ScreenHelper
import java.io.*
import java.time.LocalDateTime
import kotlin.collections.ArrayList

class ItemsListFragment : Fragment(), AdapterView.OnItemClickListener {
    private var arrayAdapter: ArrayAdapter<Note>? = null
    private var listView: ListView? = null
    private lateinit var notes: ArrayList<Note>
    private var selectedItemPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notes = DataProvider.notes
        activity?.let {
            arrayAdapter = ArrayAdapter(
                it,
                android.R.layout.simple_list_item_activated_1,
                notes
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_items_list, container, false)
        DataProvider.clear()
        //writeDirEntry("", DirEntry(mutableListOf()))  // uncomment this to clear record of all root entries

        listView = view.findViewById(R.id.list_view)
        listView?.let {
            it.adapter = arrayAdapter
            it.choiceMode = ListView.CHOICE_MODE_SINGLE
            it.onItemClickListener = this
        }

        view.findViewById<FloatingActionButton>(R.id.add_fab).setOnClickListener {
            // Set selected item to newly created note (first element in list)
            startNoteFragment(addDirEntry(""))
        }

        //DataProvider.clear()
        loadDirectory("")

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setSelectedItem(position: Int) {
        listView?.setItemChecked(position, true)
        selectedItemPosition = position
    }

    override fun onItemClick(adapterView: AdapterView<*>, item: View, position: Int, rowId: Long) {
        startNoteFragment(position)
    }

    private fun startNoteFragment(position: Int) {
        val note = arrayAdapter?.getItem(position)
        setSelectedItem(position)

        note?.let {
            activity?.let { activity ->
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

    fun updateNote(title: String, text: String) {
        // REVISIT: assuming that currently selected item is the note that just got updated
        var note: Note?

        arrayAdapter?.let { array ->
            note = array.getItem(selectedItemPosition)

            note?.let {
                it.title = title
                it.text = text
                it.dateModified = LocalDateTime.now()
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

    private fun loadDirectory(subDir: String) {
        readDirEntry(subDir)?.let {notes ->
            for (i in notes.ids) {
                DataProvider.createNote(i)
                loadNote(subDir, "/n" + i.toString())?.let {note ->
                    DataProvider.addNote(note)
                }
            }
        }
    }

    private fun addDirEntry(subDir: String): Int {
        var id = 0
        readDirEntry(subDir)?.let { entry ->
            id = entry.ids[entry.ids.lastIndex] + 1
            entry.ids.add(id)
            writeDirEntry(subDir, entry)
            return DataProvider.createNote(id)
        }
        val newEntry = DirEntry(mutableListOf())
        newEntry.ids.add(id)
        writeDirEntry(subDir, newEntry)
        return DataProvider.createNote(id)
    }

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
                Log.e(this.javaClass.toString(), "not a note")
                return null
            }
        } catch (e: FileNotFoundException) {
            Log.e(this.javaClass.toString(), e.message.toString())
            return null
        } finally {
            objectStream?.close()
            fileStream?.close()
        }
    }

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
                Log.e(this.javaClass.toString(), "Error: dEntry not an Int")
                return null
            }
        } catch (e: FileNotFoundException) {
            val entry = DirEntry(mutableListOf())
            writeDirEntry(subDir, entry)    // create a new dir entry
            return entry
        } finally {
            objectStream?.close()
            fileStream?.close()
        }
    }

    private fun writeDirEntry(subDir: String, entry: DirEntry) {
        val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
        val file = File(path + subDir + "/dEntry")
        val fileStream = FileOutputStream(file)
        val objectStream = ObjectOutputStream(fileStream)
        objectStream.writeObject(entry)
        objectStream.close()
        fileStream.close()
    }
}
