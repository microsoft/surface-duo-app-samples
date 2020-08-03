/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.device.display.samples.twonote.includes.DragHandler
import com.microsoft.device.display.samples.twonote.model.DataProvider
import com.microsoft.device.display.samples.twonote.model.DrawViewModel
import com.microsoft.device.display.samples.twonote.model.INode
import com.microsoft.device.display.samples.twonote.model.Note
import com.microsoft.device.dualscreen.core.ScreenHelper
import java.io.File
import java.io.FileOutputStream
import java.lang.ClassCastException
import java.time.LocalDateTime

class NoteDetailFragment : Fragment() {
    enum class PaintColors { Red, Blue, Green, Yellow, Purple }

    private lateinit var drawView: PenDrawView
    lateinit var noteText: TextInputEditText
    private lateinit var noteTitle: TextInputEditText
    private lateinit var rootDetailLayout: ConstraintLayout
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
        fun onINodeUpdate()
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
        noteTitle = view.findViewById(R.id.title_input)
        noteText = view.findViewById(R.id.text_input)
        rootDetailLayout = view.findViewById(R.id.note_detail_layout)

        addNoteContents()
        setUpInkMode(view)
        setUpTools(view)
        initializeDragListener()

        return view
    }

    private fun addNoteContents() {
        val note: Note? = arguments?.let {
            val note = it.getSerializable(MainActivity.NOTE)

            if (note is Note) note
            else null
        }
        noteTitle.setText(note?.title)
        noteText.setText(note?.text)
    }

    fun updateNoteContents() {
        arguments?.let {
            val note = it.getSerializable(MainActivity.NOTE)
            val inode = it.getSerializable(MainActivity.INODE)
            if (note is Note && inode is INode && !deleted) {
                val text = noteText.text.toString()
                val title = noteTitle.text.toString()

                if (this::drawView.isInitialized) {
                    note.drawings = drawView.getDataList()
                }

                note.text = text
                note.title = title
                inode.title = title
                inode.dateModified = LocalDateTime.now()

                mListener.onINodeUpdate()
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
        thickness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // Progress: [0, 6] (default 3), Thicknesses: [5, 100] (default 25)
                val newThickness =
                    when (progress) {
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

        val chooseButton = view.findViewById<ImageButton>(R.id.button_choose)
        chooseButton.setOnClickListener {
            val textInput = TextInputEditText(requireContext())

            AlertDialog.Builder(requireContext())
                .setMessage(resources.getString(R.string.choose_color_message))
                .setView(textInput)
                .setCancelable(true)
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                    val result = stringToColor(textInput.text.toString())
                    if (result != -1) {
                        chooseColor("", result)
                        chooseButton.background.colorFilter = PorterDuffColorFilter(result, PorterDuff.Mode.SRC)
                    } else {
                        chooseButton.background.clearColorFilter()
                    }
                    dialog.dismiss()
                }
                .setTitle(resources.getString(R.string.choose_color))
                .create()
                .show()
        }

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

    /**
     * Converts user-inputted color to Color object using parseColor method
     *
     * Accepted hexadecimal color formats: #RRGGBB or #AARRGGBB
     *
     * Accepted color names: red, blue, green, black, white, gray, cyan, magenta, yellow,
     * lightgray, darkgray, grey, lightgrey, darkgrey, aqua, fuchsia, lime, maroon,
     * navy, olive, purple, silver, and teal.
     *
     */
    private fun stringToColor(string: String): Int {
        return try {
            Color.parseColor(string)
        } catch (e: Exception) {
            -1
        }
    }

    private fun toggleViewVisibility(view: View?, hide: Boolean = false) {
        if (view?.visibility == View.VISIBLE || hide) {
            view?.visibility = View.INVISIBLE
        } else {
            view?.visibility = View.VISIBLE
        }
    }

    private fun undoStroke() {
        drawView.undo()
    }

    override fun onPause() {
        super.onPause()
        updateNoteContents()
        save()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::drawView.isInitialized) {
            drawView.viewTreeObserver.removeOnDrawListener { }
        }
    }

    private fun chooseColor(color: String, colorInt: Int? = null) {
        when (color) {
            PaintColors.Red.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.red))
            PaintColors.Blue.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.blue))
            PaintColors.Green.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.green))
            PaintColors.Yellow.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.yellow))
            PaintColors.Purple.name -> drawView.changePaintColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.purple))
            else -> if (colorInt != null) drawView.changePaintColor(colorInt)
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
                FileSystem.save(requireContext(), DataProvider.getActiveSubDirectory(), note)
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
                view?.let {
                    // Create path for note image file
                    val inode = arguments?.getSerializable(MainActivity.INODE) as? INode
                    val path = requireContext().getExternalFilesDir(null)?.absolutePath + "/$inode.jpg"

                    // Get location of NoteDetailFragment view within window
                    val coords = IntArray(2)
                    it.getLocationInWindow(coords)

                    // Screenshot NoteDetailFragment and store it as a bitmap
                    val bitmap = Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
                    PixelCopy.request(
                        requireActivity().window,
                        Rect(coords[0], coords[1], coords[0] + it.width, coords[1] + it.height),
                        bitmap,
                        { copyResult: Int ->
                            // If stored successfully, open an Intent for sharing the note as an image
                            if (copyResult == PixelCopy.SUCCESS) {
                                openShareIntent(bitmap, path)
                            }
                        },
                        it.handler
                    )
                }
                true
            }
            R.id.action_delete -> {
                arguments?.let {
                    val inode = it.getSerializable(MainActivity.INODE)
                    if (inode is INode) {
                        FileSystem.delete(requireContext(), DataProvider.getActiveSubDirectory(), inode)
                        deleted = true
                    }
                }
                closeFragment()
                true
            }
            R.id.action_ink -> {
                val penTools = view?.findViewById<LinearLayout>(R.id.pen_tools)

                // Toggle between inking/text mode
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
                    toggleViewVisibility(view?.findViewById<SeekBar>(R.id.thickness_slider), true)
                    toggleViewVisibility(view?.findViewById<LinearLayout>(R.id.color_buttons), true)
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun openShareIntent(bitmap: Bitmap, path: String) {
        // Write bitmap to file (with parameter 100 for max quality)
        val outputStream = FileOutputStream(path)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        // Open Intent for sharing the image
        val file = File(path)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file))
        startActivity(Intent.createChooser(intent, resources.getString(R.string.share_intent)))
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
            if (!ScreenHelper.isDualMode(it) ||(ScreenHelper.isDualMode(it) && MainActivity.isRotated(requireContext()))) {
                // If unspanned, or spanned and rotated (extended view), show NoteListFragment in first container
                parentFragmentManager.beginTransaction()
                    .replace(R.id.first_container_id, NoteListFragment(), MainActivity.LIST_FRAGMENT)
                    .commit()
            } else {
                // If spanned and not rotated (list/detail view), show GetStartedFragment in second container
                parentFragmentManager.beginTransaction()
                    .replace(R.id.second_container_id, GetStartedFragment(), null)
                    .commit()
            }
        }
    }

    // create drop targets for the editor screen
    private fun initializeDragListener() {
        val handler = DragHandler(requireActivity(), noteText, requireActivity().contentResolver)

        // Main target will trigger when textField has content
        noteText.setOnDragListener { _, event ->
            handler.onDrag(event)
        }

        // Sub target will trigger when textField is empty
        rootDetailLayout.setOnDragListener { _, event ->
            handler.onDrag(event)
        }
    }
}
