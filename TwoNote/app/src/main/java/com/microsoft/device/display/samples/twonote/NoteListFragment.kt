/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import Defines.DETAIL_FRAGMENT
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.device.display.samples.twonote.model.DataProvider
import com.microsoft.device.display.samples.twonote.model.DirEntry
import com.microsoft.device.display.samples.twonote.model.INode
import com.microsoft.device.display.samples.twonote.model.Note
import com.microsoft.device.dualscreen.core.ScreenHelper

class NoteListFragment : Fragment(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener {
    private var arrayAdapter: ArrayAdapter<INode>? = null
    private var dropDownAdapter: ArrayAdapter<INode>? = null
    private var listView: ListView? = null
    private var categoryView: Spinner? = null
    private lateinit var inodes: MutableList<INode>
    private lateinit var categories: MutableList<INode>
    private lateinit var editText: TextInputEditText
    private val root = ""
    private var selectedFlag = false
    private var noteSelectionListener: NoteSelectionListener? = null

    companion object {
        const val LIST_VIEW = "list view"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inodes = DataProvider.getINodes()
        categories = DataProvider.getCategories()
        FileSystem.loadCategories(requireContext(), root)

        activity?.let {
            arrayAdapter = object : ArrayAdapter<INode>(it, android.R.layout.simple_list_item_2, android.R.id.text1, inodes) {
                // Override getView function so that ArrayAdapter can be used while both text
                // views in the simple_list_item_2 format are updated
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)

                    val text1 = view.findViewById<TextView>(android.R.id.text1)
                    text1.text = inodes[position].title
                    text1.setTypeface(null, Typeface.BOLD)

                    val text2 = view.findViewById<View>(android.R.id.text2) as TextView
                    text2.text = inodes[position].dateModifiedString()
                    text2.setTextColor(it.getColor(R.color.colorOnBackgroundVariant))

                    return view
                }
            }
            dropDownAdapter = object : ArrayAdapter<INode>(it, android.R.layout.simple_spinner_item, android.R.id.text1, categories) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)

                    if (categories.isNotEmpty()) {
                        val text1 = view.findViewById<TextView>(android.R.id.text1)
                        text1.text = ""
                    }

                    return view
                }
            }
            dropDownAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // uncomment this to clear record of all root entries (use for testing)
        /* val cat = DataProvider.getCategories()
        for (nodes in cat.size - 1 downTo 0) {
            FileHandler.switchCategory(requireContext(), cat[0])
            val n = DataProvider.getINodes()
            for (notes in n.size - 1 downTo 0) {
                FileHandler.delete(requireContext(), ROOT, n[notes])
                n.removeAt(notes)
            }
            FileHandler.delete(requireContext(), ROOT, cat[nodes])
            cat.removeAt(nodes)
        }
        FileHandler.writeDirEntry(requireContext(), ROOT, DirEntry())
        FileHandler.addCategory(requireContext())*/
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(LIST_VIEW, listView?.onSaveInstanceState())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)

        editText = view.findViewById(R.id.title_list_input)
        editText.setText(DataProvider.getActiveCategoryName())
        setOnChangeListenerForTextInput(editText)

        listView = view.findViewById(R.id.list_view)
        listView?.let {
            it.adapter = arrayAdapter
            it.onItemClickListener = this
            it.onItemLongClickListener = this
            noteSelectionListener = NoteSelectionListener(this, it, arrayAdapter!!)
            it.setMultiChoiceModeListener(noteSelectionListener)
            it.choiceMode = ListView.CHOICE_MODE_SINGLE

            // REVISIT: is this necessary
            if (savedInstanceState != null)
                listView?.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_VIEW))
        }

        categoryView = view.findViewById(R.id.dropdown_spinner)
        categoryView?.let {
            it.adapter = dropDownAdapter
            it.onItemSelectedListener = this
        }

        view.findViewById<FloatingActionButton>(R.id.add_fab).setOnClickListener {
            // Set selected item to newly created note (first element in list)
            FileSystem.addInode()
            updateArrayAdapter()
            startNoteFragment(0)
        }

        // Set up toolbar icons and actions
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.inflateMenu(R.menu.menu_note_list)
        toolbar.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }

        requireContext().let {
            toolbar.setNavigationIcon(R.drawable.ic_icon_unfilled)
            toolbar.navigationIcon?.setTint(it.getColor(R.color.colorOnPrimary))

            // Set overflow icon color
            toolbar.overflowIcon?.setTint(it.getColor(R.color.colorOnPrimary))
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        FileSystem.writeDirEntry(requireContext(), DataProvider.getActiveSubDirectory(), DirEntry(inodes))
        FileSystem.writeDirEntry(requireContext(), root, DirEntry(categories))
    }

    override fun onItemSelected(adapterView: AdapterView<*>, item: View?, position: Int, id: Long) {
        if (selectedFlag) {
            dropDownAdapter?.let {
                it.getItem(position)?.let { inode ->
                    setNewCategory(inode, false)
                    editText.setText(inode.title)
                }
            }
        }
        selectedFlag = !selectedFlag
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // TODO: implement this
    }

    override fun onItemClick(adapterView: AdapterView<*>, item: View, position: Int, rowId: Long) {
        if (listView?.choiceMode == ListView.CHOICE_MODE_SINGLE) {
            startNoteFragment(position)
        } else {
            listView?.setItemChecked(position, true)
        }
    }

    // listener for changes to text in code editor
    private fun setOnChangeListenerForTextInput(field: TextInputEditText) {
        field.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                DataProvider.setActiveCategoryName(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setNewCategory(inode: INode?, deleting: Boolean) {
        exitDetailFragment(deleting)
        FileSystem.writeDirEntry(requireContext(), DataProvider.getActiveSubDirectory(), DirEntry(inodes))
        FileSystem.switchCategory(requireContext(), inode)
        updateDropDown()
        updateArrayAdapter()
        categoryView?.setSelection(0)
    }

    private fun startNoteFragment(position: Int) {
        arrayAdapter?.getItem(position)?.let { inode ->
            DataProvider.moveINodeToTop(inode)
            updateArrayAdapter()
            listView?.setItemChecked(position, true)

            var note = FileSystem.loadNote(requireContext(), DataProvider.getActiveSubDirectory(), inode.descriptor + inode.id)
            if (note == null)
                note = Note(inode.id)

            if (ScreenHelper.isDualMode(requireActivity()) && !MainActivity.isRotated(requireActivity())) {
                // If spanned and not rotated (list view), open NoteDetailFragment in second container
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.second_container_id,
                        NoteDetailFragment.newInstance(inode, note),
                        DETAIL_FRAGMENT
                    ).commit()
            } else {
                // If unspanned or spanned and rotated (extended view), open NoteDetailFragment in first container
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.first_container_id,
                        NoteDetailFragment.newInstance(inode, note),
                        DETAIL_FRAGMENT
                    ).addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_category -> {
                setNewCategory(null, false)
                editText.setText(DataProvider.getActiveCategoryName())
                true
            }
            R.id.action_delete_category -> {
                if (DataProvider.getCategories().size > 1) {
                    DataProvider.clearInodes()
                    setNewCategory(DataProvider.getCategories()[1], true)

                    val categoryToDelete = DataProvider.getCategories()[1]
                    FileSystem.delete(requireContext(), root, categoryToDelete)
                    DataProvider.removeCategory(categoryToDelete)

                    editText.setText(DataProvider.getActiveCategoryName())
                    updateDropDown()
                }
                true
            }
            R.id.action_select -> {
                // Select all notes
                listView?.let {
                    it.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
                    for (i in 0 until it.count) {
                        it.setItemChecked(i, true)
                    }
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onItemLongClick(adapterView: AdapterView<*>, item: View, position: Int, rowId: Long): Boolean {
        listView?.let {
            it.clearChoices()
            it.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
            it.setItemChecked(position, true)
        }
        return true
    }

    fun exitDetailFragment(deleting: Boolean) {
        activity?.let {
            if (ScreenHelper.isDualMode(it) && !MainActivity.isRotated(it)) {
                val fragment = parentFragmentManager.findFragmentById(R.id.second_container_id) as? NoteDetailFragment

                fragment?.let { detail ->
                    if (!deleting) {
                        detail.updateNoteContents()
                        detail.save()
                    }
                    detail.deleted = true // set flag so file isn't resaved on destroy
                    detail.closeFragment()
                }
            }
        }
    }

    fun updateArrayAdapter() {
        arrayAdapter?.notifyDataSetChanged()
    }

    private fun updateDropDown() {
        dropDownAdapter?.notifyDataSetChanged()
    }
}
