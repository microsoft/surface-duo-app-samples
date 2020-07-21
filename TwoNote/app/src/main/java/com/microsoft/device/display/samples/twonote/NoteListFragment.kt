/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.microsoft.device.display.samples.twonote.model.DataProvider
import com.microsoft.device.display.samples.twonote.model.DirEntry
import com.microsoft.device.display.samples.twonote.model.INode
import com.microsoft.device.display.samples.twonote.model.Note
import com.microsoft.device.dualscreen.layout.ScreenHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.time.LocalDateTime

class NoteListFragment : Fragment(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private var arrayAdapter: ArrayAdapter<INode>? = null
    private var listView: ListView? = null
    private lateinit var inodes: MutableList<INode>
    private var selectedItemPosition: Int = 0
    private val ROOT = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inodes = DataProvider.inodes
        activity?.let {
            arrayAdapter = object : ArrayAdapter<INode>(it, android.R.layout.simple_list_item_2, android.R.id.text1, inodes) {
                // Override getView function so that ArrayAdapter can be used while both text
                // views in the simple_list_item_2 format are updated
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)

                    val text1 = view.findViewById<View>(android.R.id.text1) as TextView
                    text1.text = inodes[position].title
                    text1.setTypeface(null, Typeface.BOLD)

                    val text2 = view.findViewById<View>(android.R.id.text2) as TextView
                    text2.text = inodes[position].dateModifiedString()
                    text2.setTextColor(it.getColor(R.color.colorOnBackgroundVariant))

                    return view
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)
        // writeDirEntry("", DirEntry())  // uncomment this to clear record of all root entries (use for testing)

        listView = view.findViewById(R.id.list_view)
        listView?.let {
            it.adapter = arrayAdapter
            it.choiceMode = ListView.CHOICE_MODE_SINGLE
            it.onItemClickListener = this
            it.onItemLongClickListener = this
        }

        view.findViewById<FloatingActionButton>(R.id.add_fab).setOnClickListener {
            // Set selected item to newly created note (first element in list)
            val inode = addInode(ROOT)
            arrayAdapter?.notifyDataSetChanged()
            startNoteFragment(inode)
        }

        loadDirectory(ROOT)

        // Set up toolbar icons and actions
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.inflateMenu(R.menu.menu_note_list)
        toolbar.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }

        context?.let {
            // TODO: switch to colored icon
            toolbar.setNavigationIcon(R.drawable.ic_icon_unfilled)
            toolbar.navigationIcon?.setTint(it.getColor(R.color.colorOnPrimary))
        }

        // TODO: once category tabs have been implemented, connect to toolbar title here
        toolbar.title = "Category 1"

        return view
    }

    override fun onPause() {
        super.onPause()
        writeDirEntry(ROOT, DirEntry(DataProvider.inodes))
    }

    private fun setSelectedItem(position: Int) {
        listView?.setItemChecked(position, true)
        selectedItemPosition = position
    }

    override fun onItemClick(adapterView: AdapterView<*>, item: View, position: Int, rowId: Long) {
        if (listView?.choiceMode == ListView.CHOICE_MODE_SINGLE) {
            startNoteFragment(position)
        } else {
            // REVISIT: selectedItemPosition variable needs to be debugged/changed
            setSelectedItem(position)
        }

        // arrayAdapter?.getItem(position)?.let {inode ->
        // delete(ROOT, inode)
        // }
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
                            NoteDetailFragment.newInstance(note), null
                        ).commit()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.single_screen_container_id,
                            NoteDetailFragment.newInstance(note), null
                        ).addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    fun updateINode(title: String) {
        var inode: INode?

        arrayAdapter?.let { array ->
            inode = array.getItem(selectedItemPosition)

            inode?.let {
                DataProvider.inodes.remove(it)
                it.title = title
                it.dateModified = LocalDateTime.now()
                DataProvider.inodes.add(selectedItemPosition, it)
            }
            writeDirEntry(ROOT, DirEntry(DataProvider.inodes))
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
        if (DataProvider.inodes.isEmpty()) {
            readDirEntry(subDir)?.let { notes ->
                for (inode in notes.inodes) {
                    DataProvider.addINode(inode)
                }
                arrayAdapter?.notifyDataSetChanged()
            }
        }
    }

    // add a new inode
    private fun addInode(subDir: String): Int {
        val inode = INode()
        if (DataProvider.inodes.isNotEmpty()) {
            inode.id = DataProvider.inodes[DataProvider.inodes.lastIndex].id + 1
            inode.title = "Note " + inode.id
            DataProvider.addINode(inode)
            return DataProvider.inodes.lastIndex
        } else {
            DataProvider.addINode(inode)
            return DataProvider.inodes.lastIndex
        }
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
        } catch (e: Exception) {
            Log.e(this.javaClass.toString(), e.message.toString())
            return null
        } finally {
            objectStream?.close()
            fileStream?.close()
        }
    }

    // remove an inode and its associated note
    private fun delete(subDir: String, inode: INode): Boolean {
        val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
        val file = File(path + subDir + "/n" + inode.id)

        if (!DataProvider.inodes.isNullOrEmpty()) {
            DataProvider.removeINode(inode)
            arrayAdapter?.notifyDataSetChanged()
            setSelectedItem(0)

            if (file.exists()) {
                file.delete()
                return true
            }
        }
        return false
    }

    // reads directory entry to get inodes
    private fun readDirEntry(subDir: String): DirEntry? {
        val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
        val file = File("$path$subDir/dEntry")
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
        } catch (e: Exception) {
            val entry = DirEntry()
            writeDirEntry(subDir, entry) // create a new dir entry
            return entry
        } finally {
            objectStream?.close()
            fileStream?.close()
        }
    }

    // update/create directory entry
    fun writeDirEntry(subDir: String, entry: DirEntry) {
        val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
        val file = File("$path$subDir/dEntry")
        val fileStream = FileOutputStream(file)
        val objectStream = ObjectOutputStream(fileStream)
        objectStream.writeObject(entry)
        objectStream.close()
        fileStream.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select -> {
                // TODO: add selection bubbles to all notes and pop up contextual toolbar, return true when implemented
                initListViewMultipleMode(listView)

                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onItemLongClick(adapterView: AdapterView<*>, item: View, position: Int, rowId: Long): Boolean {
        initListViewMultipleMode(listView)
        setSelectedItem(position)

        return true
    }

    private fun initListViewMultipleMode(listView: ListView?) {
        listView?.let {
            it.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
            it.setMultiChoiceModeListener(NoteSelectionListener(this, it))
        }
    }
}
