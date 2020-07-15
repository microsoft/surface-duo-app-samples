/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.os.Bundle
import android.util.Log
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
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class NoteFragment : Fragment() {
    companion object {
        internal fun newInstance(noteName: String) = NoteFragment().apply {
            arguments = Bundle().apply {
                this.putString("note", noteName)
            }
        }
    }

    enum class PaintColors {
        Red,
        Blue,
        Green,
        Yellow,
        Purple
    }

    private lateinit var drawView: PenDrawView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_items_note, container, false)

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

        setUpInkMode(view)
        setUpTextMode(view)

        return view
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

        recoverDrawing()

        drawView.viewTreeObserver.addOnDrawListener {
            copyDrawBitmapIfAdded()
        }
        drawView.setPaintRadius(0)
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onPause() {
        super.onPause()
        save()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::drawView.isInitialized) {
            drawView.viewTreeObserver.removeOnDrawListener { }
        }
    }

    // Drawing related
    private fun chooseColor(title: String) {
        when (title) {
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
        val angle = viewModel.getPenRadius()
        drawView.setPaintRadius(angle)
    }

    private fun clearDrawing() {
        drawView.clearDrawing()
        val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)
        viewModel.setImageLiveData(null)
        viewModel.setStrokeList(listOf())
    }

    private fun copyDrawBitmap() {
        val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)
        val drawBitmap = drawView.getDrawBitmap()
        if (drawBitmap != null) {
            viewModel.setImageLiveData(drawBitmap)
        }
        val strokeList = drawView.getStrokeList()
        if (strokeList.isNotEmpty()) {
            viewModel.setStrokeList(strokeList)
        }
    }

    private fun copyDrawBitmapIfAdded() {
        if (isAdded) {
            copyDrawBitmap()
        }
    }

    private fun save() {
        arguments?.let {
            val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
            val file = File(path + "/" + it.getString("note"))
            val fileStream = FileOutputStream(file)
            val objectStream = ObjectOutputStream(fileStream)
            objectStream.writeObject(drawView.getDataList())
            objectStream.close()
            fileStream.close()
        }
    }

    private fun load() {
        arguments?.let {
            val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
            val file = File(path + "/" + it.getString("note"))
            var fileStream: FileInputStream? = null
            var objectStream: ObjectInputStream? = null

            try {
                fileStream = FileInputStream(file)
                objectStream = ObjectInputStream(fileStream)
                val obj = objectStream.readObject() as List<SerializedStroke>
                val strokeList: MutableList<Stroke> = mutableListOf()
                for (s in obj) {
                    strokeList.add(Stroke(s.xList, s.yList, s.pressureList, s.paintColor))
                }

                val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)
                viewModel.setStrokeList(strokeList)
                recoverDrawing()
            } catch (e: FileNotFoundException) {
                Log.e(this.javaClass.toString(), e.message.toString())
            } finally {
                objectStream?.close()
                fileStream?.close()
            }
        }
    }
}
