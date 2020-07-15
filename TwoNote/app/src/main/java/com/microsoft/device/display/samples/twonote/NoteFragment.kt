/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.microsoft.device.display.samples.twonote.model.DrawViewModel
import java.io.*

class NoteFragment : Fragment() {

    enum class PaintColors {
        Red,
        Blue,
        Green,
        Yellow,
        Purple
    }
    private lateinit var drawView: PenDrawView
    private lateinit var textView: TextView

    // Life cycle
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
                R.layout.fragment_items_note,
                container,
                false
        )
        textView = view.findViewById(R.id.textView)
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

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        drawView.viewTreeObserver.removeOnDrawListener { }
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

        load()
    }

    private fun recoverDrawing() {
        val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)
        val strokes = viewModel.getStrokeList()
        drawView.setStrokeList(strokes)
        val angle = viewModel.getPenRadius()
        drawView.setPaintRadius(angle)
    }

    private fun clearDrawing() {
        save()
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
        val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
        val file = File(path + "/test0")
        val fileStream = FileOutputStream(file)
        val objectStream = ObjectOutputStream(fileStream)
        objectStream.writeObject(drawView.getDataList())
        objectStream.close()
        fileStream.close()
    }

    private fun load() {
        val path: String? = requireContext().getExternalFilesDir(null)?.absolutePath
        val file = File(path + "/test0")
        val fileStream = FileInputStream(file)
        val objectStream = ObjectInputStream(fileStream)
        val obj = objectStream.readObject() as List<SerializedStroke>
        val strokeList: MutableList<Stroke> = mutableListOf()
        for (strokes in 0 until obj.size) {
            val s = obj[strokes]
            strokeList.add(Stroke(s.xList, s.yList, s.pressureList, s.paintColor))
        }

        val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)
        viewModel.setStrokeList(strokeList)
        recoverDrawing()

        objectStream.close()
        fileStream.close()
    }
}
