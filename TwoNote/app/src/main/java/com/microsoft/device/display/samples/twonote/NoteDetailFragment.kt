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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.device.display.samples.twonote.model.DataProvider
import com.microsoft.device.display.samples.twonote.model.DrawViewModel
import com.microsoft.device.display.samples.twonote.model.INode
import com.microsoft.device.display.samples.twonote.model.Note
import com.microsoft.device.dualscreen.core.ScreenHelper
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

        const val THICKNESS_1 = 5
        const val THICKNESS_2 = 15
        const val THICKNESS_DEFAULT = 25
        const val THICKNESS_4 = 50
        const val THICKNESS_5 = 75
        const val THICKNESS_6 = 100
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
        setUpInkMode(view)
        setUpTools(view)

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
        // Set up toolbar icons and actions
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.inflateMenu(R.menu.menu_note_detail)
        toolbar.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        toolbar.overflowIcon?.setTint(requireContext().getColor(R.color.colorOnPrimary))

//        val penTools = view.findViewById<LinearLayout>(R.id.pen_tools)
//        penTools.inflateMenu(R.menu.menu_pen_tools)
//        penTools.setOnMenuItemClickListener {
//            onOptionsItemSelected(it)
//        }

        view.findViewById<ScrollView>(R.id.text_mode)?.bringToFront()
    }

    private fun setUpInkMode(view: View) {
        drawView = view.findViewById(R.id.draw_view)

        val clearButton = view.findViewById<MaterialButton>(R.id.clear)
        clearButton.setOnClickListener { clearDrawing() }

        val undoButton = view.findViewById<MaterialButton>(R.id.undo)
        undoButton.setOnClickListener { undoStroke() }

        val colorButton = view.findViewById<MaterialButton>(R.id.color)
        colorButton.setOnClickListener { toggleViewVisibility(view.findViewById(R.id.color_buttons)) }

        val thickness = view.findViewById<SeekBar>(R.id.thickness_slider)
        thickness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // Progress: [0, 6] (default 3), Thicknesses: [5, 100] (default 25)
                val newThickness = when (progress) {
                    1 -> THICKNESS_1
                    2 -> THICKNESS_2
                    3 -> THICKNESS_DEFAULT
                    4 -> THICKNESS_4
                    5 -> THICKNESS_5
                    6 -> THICKNESS_6
                    else -> THICKNESS_DEFAULT
                }
                drawView.changeThickness(newThickness)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {}

            override fun onStopTrackingTouch(seek: SeekBar) {}
        })

        val thicknessButton = view.findViewById<MaterialButton>(R.id.thickness)
        thicknessButton.setOnClickListener { toggleViewVisibility(thickness) }

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
                    strokeList.add(Stroke(s.xList, s.yList, s.pressureList, s.paintColor, s.thicknessMultiplier))
                }
                viewModel.setStrokeList(strokeList)
            }
        }

        recoverDrawing()

        drawView.viewTreeObserver.addOnDrawListener {
            copyDrawingIfAdded()
        }
        drawView.setPaintRadius(0)

        // REVISIT: connect to savedInstanceState bundle
        drawView.disable()
    }

    private fun toggleViewVisibility(view: View?) {
        if (view?.visibility == View.VISIBLE) {
            view.visibility = View.INVISIBLE
        } else {
            view?.visibility = View.VISIBLE
        }
    }

    private fun undoStroke() {
        drawView.undo()
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
            R.id.action_ink -> {
                val penTools = view?.findViewById<LinearLayout>(R.id.pen_tools)

                if (drawView.isDisabled()) {
                    drawView.enable()
                    view?.findViewById<ConstraintLayout>(R.id.ink_mode)?.bringToFront()
                    item.setIcon(R.drawable.ic_text)
                    item.title = getString(R.string.action_ink_off)
                    penTools?.visibility = View.VISIBLE
                    penTools?.bringToFront()

                } else {
                    drawView.disable()
                    view?.findViewById<ScrollView>(R.id.text_mode)?.bringToFront()
                    item.setIcon(R.drawable.ic_fluent_calligraphy_pen_24_filled)
                    item.title = getString(R.string.action_ink_on)
                    penTools?.visibility = View.INVISIBLE
                }

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
                        R.id.second_container_id,
                        GetStartedFragment(),
                        null
                    ).commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.first_container_id,
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
                    .replace(R.id.second_container_id, GetStartedFragment(), null)
                    .commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.first_container_id, NoteListFragment(), MainActivity.LIST_FRAGMENT)
                    .commit()
            }
        }
    }
}
