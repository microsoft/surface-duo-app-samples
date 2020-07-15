/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.os.Bundle
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
import kotlin.collections.ArrayList

class ItemsListFragment : Fragment(), AdapterView.OnItemClickListener {
    private var arrayAdapter: ArrayAdapter<Note>? = null
    private var listView: ListView? = null
    private lateinit var notes: ArrayList<Note>

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
        listView = view.findViewById(R.id.list_view)
        listView?.let {
            it.adapter = arrayAdapter
            it.choiceMode = ListView.CHOICE_MODE_SINGLE
            it.onItemClickListener = this
        }

        view.findViewById<FloatingActionButton>(R.id.add_fab).setOnClickListener {
            DataProvider.createNote(getString(R.string.untitled))
            arrayAdapter?.notifyDataSetChanged()
            startNoteDetailFragment(0)
        }

        return view
    }

    private fun setSelectedItem(position: Int) {
        listView?.setItemChecked(position, true)
    }

    override fun onItemClick(adapterView: AdapterView<*>, item: View, position: Int, rowId: Long) {
        startNoteDetailFragment(position)
    }

    private fun startNoteDetailFragment(position: Int) {
        val note = arrayAdapter?.getItem(position)
        setSelectedItem(position)
        note?.let {
            activity?.let { activity ->
                if (ScreenHelper.isDualMode(activity)) {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.dual_screen_end_container_id,
                            NoteFragment.newInstance("test1"), null
                        ).commit()
                } else {
                    startNoteFragment()
                }
            }
        }
    }

    private fun startNoteFragment() {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.single_screen_container_id,
                NoteFragment.newInstance("test1"), null
            ).addToBackStack(null)
            .commit()
    }
}
