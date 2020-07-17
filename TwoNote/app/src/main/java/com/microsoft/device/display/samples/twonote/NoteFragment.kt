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
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.device.display.samples.twonote.model.DrawViewModel
import com.microsoft.device.display.samples.twonote.model.Note
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.lang.ClassCastException

class NoteFragment : Fragment() {
    companion object {
        lateinit var mListener: OnFragmentInteractionListener

        internal fun newInstance(note: Note) = NoteFragment().apply {
            arguments = Bundle().apply {
                this.putSerializable("note", note)
            }
        }
    }

    interface OnFragmentInteractionListener {
        // TODO: add more fields (drawings? photos?) or create an object to encapsulate all the fields
        fun onINodeUpdate(title: String)
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
            throw ClassCastException(context.toString() + resources.getString(R.string.exception_message))
        }
    }

    enum class PaintColors { Red, Blue, Green, Yellow, Purple }

    private lateinit var drawView: PenDrawView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_note, container, false)

        addNoteContents(view)
        setUpTools(view)
        setUpInkMode(view)
        setUpTextMode(view)

        return view
    }

    private fun addNoteContents(view: View) {
        val note: Note? = arguments?.let {
            val note = it.getSerializable("note")

            if (note is Note) note
            else null
        }
        view.findViewById<TextInputEditText>(R.id.title_input).setText(note?.title)
        view.findViewById<TextInputEditText>(R.id.text_input).setText(note?.text)
    }

    private fun updateNoteContents(view: View?) {
        arguments?.let {
            val note = it.getSerializable("note")
            if (note is Note) {
                val text = view?.findViewById<TextInputEditText>(R.id.text_input)?.text.toString()
                val title = view?.findViewById<TextInputEditText>(R.id.title_input)?.text.toString()

                note.drawings = drawView.getDataList()
                note.text = text
                note.title = title

                mListener.onINodeUpdate(title)
            }
        }
    }

    private fun setUpTools(view: View) {
        // Sets up toggling between text/ink mode
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
    }

    // TODO: add handling so clicking inside the scrollview makes the text object in focus
    private fun setUpTextMode(view: View) {
        val input = view.findViewById<TextInputEditText>(R.id.text_input)

        input.requestFocus()
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
            val note = it.getSerializable("note")
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

    private fun save() {
        arguments?.let {
            val note = it.getSerializable("note")
            if (note is Note) {
                val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
                val file = File(path + "/n" + note.id)
                val fileStream = FileOutputStream(file)
                val objectStream = ObjectOutputStream(fileStream)
                objectStream.writeObject(note)
                objectStream.close()
                fileStream.close()
            }
        }
    }
}
