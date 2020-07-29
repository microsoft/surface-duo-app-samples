/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.device.display.samples.twonote.model.DataProvider
import com.microsoft.device.display.samples.twonote.model.DrawViewModel
import com.microsoft.device.display.samples.twonote.model.INode
import com.microsoft.device.display.samples.twonote.model.Note
import com.microsoft.device.dualscreen.layout.ScreenHelper
import java.lang.ClassCastException

class NoteDetailFragment : Fragment() {
    enum class PaintColors { Red, Blue, Green, Yellow, Purple }

    private lateinit var drawView: PenDrawView
    var deleted = false

    companion object {
        lateinit var mListener: OnFragmentInteractionListener
        internal fun newInstance(inode: INode, note: Note) = NoteDetailFragment().apply {
            arguments = Bundle().apply {
                this.putSerializable(MainActivity.NOTE, note)
                this.putSerializable(MainActivity.INODE, inode)
            }
        }
    }

    interface OnFragmentInteractionListener {
        fun onINodeUpdate(inode: INode, title: String)
    }

    /**
     * Connects this fragment to the NoteListFragment (via MainActivity) so any note edits in
     * the UI will be passed back to the actual list of note objects
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw ClassCastException("$context ${resources.getString(R.string.exception_message)}")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_note_detail, container, false)

        addNoteContents(view)
        setUpTools(view)
        setUpInkMode(view)

        return view
    }

    private fun addNoteContents(view: View) {
        val note: Note? = arguments?.let {
            val note = it.getSerializable(MainActivity.NOTE)

            if (note is Note) note
            else null
        }
        view.findViewById<TextInputEditText>(R.id.title_input).setText(note?.title)
        view.findViewById<TextInputEditText>(R.id.text_input).setText(note?.text)
    }

    fun updateNoteContents(view: View?) {
        arguments?.let {
            val note = it.getSerializable(MainActivity.NOTE)
            val inode = it.getSerializable(MainActivity.INODE)
            if (note is Note && inode is INode && !deleted) {
                val text = view?.findViewById<TextInputEditText>(R.id.text_input)?.text.toString()
                val title = view?.findViewById<TextInputEditText>(R.id.title_input)?.text.toString()

                if (this::drawView.isInitialized) {
                    note.drawings = drawView.getDataList()
                }

                note.text = text
                note.title = title

                mListener.onINodeUpdate(inode, title)
            }
        }
    }

    private fun setUpTools(view: View) {
        // Set up toggling between text/ink mode
        val text = view.findViewById<ScrollView>(R.id.text_mode)
        val ink = view.findViewById<ConstraintLayout>(R.id.ink_mode)

        val toggleButton = view.findViewById<ToggleButton>(R.id.editing_mode)
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ink.visibility = View.VISIBLE
                text.visibility = View.GONE
            } else {
                ink.visibility = View.GONE
                text.visibility = View.VISIBLE
            }
        }

        // Set up toolbar icons and actions
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.inflateMenu(R.menu.menu_note_detail)
        toolbar.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setUpInkMode(view: View) {
        drawView = view.findViewById(R.id.drawView_single)

        val clearButton = view.findViewById<Button>(R.id.button_clear)
        clearButton.setOnClickListener { clearDrawing() }

        val redButton = view.findViewById<Button>(R.id.button_red)
        redButton.setOnClickListener { chooseColor(PaintColors.Red.name) }

        val blueButton = view.findViewById<Button>(R.id.button_blue)
        blueButton.setOnClickListener { chooseColor(PaintColors.Blue.name) }

        val greenButton = view.findViewById<Button>(R.id.button_green)
        greenButton.setOnClickListener { chooseColor(PaintColors.Green.name) }

        val yellowButton = view.findViewById<Button>(R.id.button_yellow)
        yellowButton.setOnClickListener { chooseColor(PaintColors.Yellow.name) }

        val purpleButton = view.findViewById<Button>(R.id.button_purple)
        purpleButton.setOnClickListener { chooseColor(PaintColors.Purple.name) }

        arguments?.let {
            val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)
            val note = it.getSerializable(MainActivity.NOTE)
            if (note is Note) {
                val strokeList: MutableList<Stroke> = mutableListOf()
                for (s in note.drawings) {
                    strokeList.add(Stroke(s.xList, s.yList, s.pressureList, s.paintColor))
                }
                viewModel.setStrokeList(strokeList)
            }
        }

        recoverDrawing()

        drawView.viewTreeObserver.addOnDrawListener {
            copyDrawingIfAdded()
        }
        drawView.setPaintRadius(0)
    }

    override fun onPause() {
        super.onPause()
        updateNoteContents(view)
        save()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::drawView.isInitialized) {
            drawView.viewTreeObserver.removeOnDrawListener { }
        }
    }

    private fun chooseColor(color: String) {
        when (color) {
            PaintColors.Red.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.red))
            PaintColors.Blue.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.blue))
            PaintColors.Green.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.green))
            PaintColors.Yellow.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.yellow))
            PaintColors.Purple.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.purple))
        }
    }

    private fun recoverDrawing() {
        val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)
        val strokes = viewModel.getStrokeList()
        drawView.setStrokeList(strokes)
    }

    private fun clearDrawing() {
        drawView.clearDrawing()
        val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)
        viewModel.setStrokeList(listOf())
    }

    private fun copyDrawing() {
        val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)

        val strokeList = drawView.getStrokeList()
        if (strokeList.isNotEmpty()) {
            viewModel.setStrokeList(strokeList)
        }
    }

    private fun copyDrawingIfAdded() {
        if (isAdded) {
            copyDrawing()
        }
    }

    fun save() {
        arguments?.let {
            val note = it.getSerializable(MainActivity.NOTE)
            if (note is Note && !deleted) {
                FileHandler.save(requireContext(), DataProvider.getActiveSubDirectory(), note)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_move -> {
                // TODO: pop up new dialog with categories to move to, return true when implemented
                false
            }
            R.id.action_share -> {
                // TODO: return true when implemented
                // true
                false
            }
            R.id.action_delete -> {
                arguments?.let {
                    val inode = it.getSerializable(MainActivity.INODE)
                    if (inode is INode) {
                        FileHandler.delete(requireContext(), DataProvider.getActiveSubDirectory(), inode)
                        deleted = true
                    }
                }
                closeFragment()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    /**
     * Close NoteDetailFragment after deletion and open either the NoteListFragment (unspanned)
     * or the GetStartedFragment (spanned)
     */
    fun closeFragment() {
        activity?.let { activity ->
            if (ScreenHelper.isDualMode(activity)) {
                // Tell NoteListFragment that list data has changed
                (parentFragmentManager.findFragmentByTag(MainActivity.LIST_FRAGMENT) as NoteListFragment)
                    .updateArrayAdapter()

                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.dual_screen_end_container_id,
                        GetStartedFragment(),
                        null
                    ).commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.single_screen_container_id,
                        NoteListFragment(),
                        MainActivity.LIST_FRAGMENT
                    ).commit()
            }
        }
    }

    // TODO: back gesture does not get overridden by this (connected to activity's onBackPressed)
    private fun onBackPressed() {
        activity?.let {
            if (ScreenHelper.isDualMode(it)) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.dual_screen_end_container_id, GetStartedFragment(), null)
                    .commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.single_screen_container_id, NoteListFragment(), MainActivity.LIST_FRAGMENT)
                    .commit()
            }
        }
    }
}
