/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import Defines.INODE
import Defines.LIST_FRAGMENT
import Defines.NOTE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.device.display.samples.twonote.includes.DataProvider
import com.microsoft.device.display.samples.twonote.includes.DragHandler
import com.microsoft.device.display.samples.twonote.includes.FileSystem
import com.microsoft.device.display.samples.twonote.model.DrawViewModel
import com.microsoft.device.display.samples.twonote.structures.INode
import com.microsoft.device.display.samples.twonote.structures.Note
import com.microsoft.device.display.samples.twonote.structures.Stroke
import com.microsoft.device.dualscreen.core.ScreenHelper
import java.io.File
import java.io.FileOutputStream
import java.lang.ClassCastException
import java.time.LocalDateTime

class NoteDetailFragment : Fragment() {
    enum class PaintColors { Red, Blue, Green, Yellow, Purple }

    private lateinit var drawView: PenDrawView
    private lateinit var dragHandler: DragHandler

    lateinit var noteText: TextInputEditText
    private lateinit var noteTitle: TextInputEditText
    var deleted = false

    lateinit var rootDetailLayout: ConstraintLayout
    lateinit var imageContainer: RelativeLayout

    private var inkItem: MenuItem? = null
    private var textItem: MenuItem? = null
    private var imageItem: MenuItem? = null

    private var deleteImageMode = false

    companion object {
        lateinit var mListener: OnFragmentInteractionListener
        internal fun newInstance(inode: INode, note: Note) = NoteDetailFragment().apply {
            arguments = Bundle().apply {
                this.putSerializable(NOTE, note)
                this.putSerializable(INODE, inode)
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
        imageContainer = view.findViewById(R.id.image_container)

        addNoteContents()
        setUpInkMode(view)
        setUpImageMode(view)
        setUpTools(view)
        initializeDragListener()

        return view
    }

    private fun addNoteContents() {
        val note: Note? = arguments?.let {
            val note = it.getSerializable(NOTE)

            if (note is Note) note
            else null
        }
        noteTitle.setText(note?.title)
        noteText.setText(note?.text)
    }

    fun updateNoteContents() {
        arguments?.let {
            val note = it.getSerializable(NOTE)
            val inode = it.getSerializable(INODE)
            if (note is Note && inode is INode && !deleted) {
                val text = noteText.text.toString()
                val title = noteTitle.text.toString()

                if (this::drawView.isInitialized) {
                    note.drawings = drawView.getDataList()
                }
                if (this::dragHandler.isInitialized) {
                    note.images = dragHandler.getImageList()
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

        for (item in toolbar.menu) {
            if (item.title == getString(R.string.action_ink_on) ||
                item.title == getString(R.string.action_ink_off)
            )
                inkItem = item
            else if (item.title == getString(R.string.action_text_on) ||
                item.title == getString(R.string.action_text_off)
            )
                textItem = item
            else if (item.title == getString(R.string.action_image_on) ||
                item.title == getString(R.string.action_image_off)
            )
                imageItem = item
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { closeFragment() }

        toolbar.overflowIcon?.setTint(requireContext().getColor(R.color.colorOnPrimary))

        view.findViewById<ScrollView>(R.id.text_mode)?.bringToFront()
    }

    private fun setUpImageMode(view: View) {
        // Set up toolbar for image mode
        view.findViewById<ImageButton>(R.id.upload_image).setOnClickListener {}

        view.findViewById<ImageButton>(R.id.delete_image).setOnClickListener {
            Log.e("KRISTEN", "in on click listener")
            toggleDeleteImageMode()
            toggleButtonColor(it as ImageButton, deleteImageMode)
        }
    }

    private fun setUpInkMode(view: View) {
        drawView = view.findViewById(R.id.draw_view)

        // Set up pen tools buttons
        view.findViewById<ImageButton>(R.id.undo).setOnClickListener { undoStroke() }
        view.findViewById<ImageButton>(R.id.clear).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(resources.getString(R.string.confirm_clear_message))
                .setPositiveButton(resources.getString(android.R.string.ok)) { dialog, _ ->
                    clearDrawing()
                    dialog.dismiss()
                }
                .setNegativeButton(resources.getString(android.R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .setTitle(resources.getString(R.string.confirm_clear))
                .create()
                .show()
        }

        val colorButton = view.findViewById<ImageButton>(R.id.color)
        colorButton.setOnClickListener {
            val colorButtonsLayout = view.findViewById<LinearLayout>(R.id.color_buttons)
            toggleViewVisibility(colorButtonsLayout)
            toggleButtonColor(colorButton, colorButtonsLayout?.visibility == View.VISIBLE)
        }

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

        val thicknessButton = view.findViewById<ImageButton>(R.id.thickness)
        thicknessButton.setOnClickListener {
            toggleViewVisibility(thickness)
            toggleButtonColor(thicknessButton, thickness?.visibility == View.VISIBLE)
        }

        val highlightButton = view.findViewById<ImageButton>(R.id.highlight)
        val eraseButton = view.findViewById<ImageButton>(R.id.erase)

        highlightButton.setOnClickListener {
            val activate = drawView.toggleHighlightMode()
            toggleButtonColor(highlightButton, activate)

            // Turn off eraser mode if activating highlighting mode
            if (activate) {
                toggleButtonColor(eraseButton, drawView.toggleEraserMode(false))
                it.contentDescription = resources.getString(R.string.action_highlight_off)
            } else {
                it.contentDescription = resources.getString(R.string.action_highlight_on)
            }
        }

        eraseButton.setOnClickListener {
            val activate = drawView.toggleEraserMode()
            toggleButtonColor(eraseButton, activate)

            // Turn off highlight button if activating eraser mode
            if (activate) {
                toggleButtonColor(highlightButton, drawView.toggleHighlightMode(false))
                it.contentDescription = resources.getString(R.string.action_erase_off)
            } else {
                it.contentDescription = resources.getString(R.string.action_erase_on)
            }
        }

        // Set up color buttons
        view.findViewById<Button>(R.id.button_red).setOnClickListener { chooseColor(PaintColors.Red.name) }
        view.findViewById<Button>(R.id.button_blue).setOnClickListener { chooseColor(PaintColors.Blue.name) }
        view.findViewById<Button>(R.id.button_green).setOnClickListener { chooseColor(PaintColors.Green.name) }
        view.findViewById<Button>(R.id.button_yellow).setOnClickListener { chooseColor(PaintColors.Yellow.name) }
        view.findViewById<Button>(R.id.button_purple).setOnClickListener { chooseColor(PaintColors.Purple.name) }

        val chooseButton = view.findViewById<ImageButton>(R.id.button_choose)
        chooseButton.setOnClickListener {
            val textInput = TextInputEditText(requireContext())

            AlertDialog.Builder(requireContext())
                .setMessage(resources.getString(R.string.choose_color_message))
                .setView(textInput)
                .setPositiveButton(resources.getString(android.R.string.ok)) { dialog, _ ->
                    val result = stringToColor(textInput.text.toString())
                    if (result != -1) {
                        chooseColor("", result)
                        chooseButton.background.colorFilter = PorterDuffColorFilter(result, PorterDuff.Mode.SRC)
                    } else {
                        chooseButton.background.clearColorFilter()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(resources.getString(android.R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .setTitle(resources.getString(R.string.choose_color))
                .create()
                .show()
        }

        // Set up draw view
        arguments?.let {
            val viewModel = ViewModelProvider(requireActivity()).get(DrawViewModel::class.java)
            val note = it.getSerializable(NOTE)
            if (note is Note) {
                val strokeList: MutableList<Stroke> = mutableListOf()
                for (s in note.drawings) {
                    strokeList.add(Stroke(s.xList, s.yList, s.pressureList, s.paintColor, s.thicknessMultiplier, s.rotated, s.highlightStroke))
                }
                viewModel.setStrokeList(strokeList)
            }
        }

        recoverDrawing()

        drawView.viewTreeObserver.addOnDrawListener {
            copyDrawingIfAdded()
        }

        drawView.setPaintRadius(0)
        drawView.rotated = MainActivity.isRotated(requireActivity())
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
            Color.parseColor(string.trim())
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

    private fun toggleButtonColor(button: ImageButton?, activated: Boolean) {
        if (activated) {
            button?.background?.colorFilter = PorterDuffColorFilter(resources.getColor(R.color.colorPrimary, requireActivity().theme), PorterDuff.Mode.SRC)
        } else {
            button?.background?.clearColorFilter()
        }
    }

    private fun toggleDeleteImageMode(force: Boolean? = null) {
        deleteImageMode = force ?: !deleteImageMode

        val alpha = if (deleteImageMode) 0.5f else 1f

        for (image in dragHandler.getImageViewList()) {
            image.alpha = alpha
        }

        dragHandler.setDeleteMode(deleteImageMode)
    }

    private fun undoStroke() {
        drawView.undo()
    }

    override fun onResume() {
        super.onResume()
        arguments?.let {
            val note = it.getSerializable(NOTE)
            if (note is Note) {
                val imageList = note.images
                dragHandler.setImageList(imageList, MainActivity.isRotated(requireContext()))
            }
        }
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
            val note = it.getSerializable(NOTE)
            if (note is Note && !deleted) {
                FileSystem.save(requireContext(), DataProvider.getActiveSubDirectory(), note)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                view?.let {
                    // Create path for note image file
                    val inode = arguments?.getSerializable(INODE) as? INode
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
                    val inode = it.getSerializable(INODE)
                    if (inode is INode) {
                        FileSystem.delete(requireContext(), DataProvider.getActiveSubDirectory(), inode)
                        deleted = true
                    }
                }
                closeFragment()
                true
            }
            R.id.action_text -> {
                activateText(true)
                activateImage(false)
                activateInk(false)
                true
            }
            R.id.action_image -> {
                activateText(false)
                activateImage(true)
                activateInk(false)
                true
            }
            R.id.action_ink -> {
                activateText(false)
                activateImage(false)
                activateInk(true)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    fun activateText(active: Boolean) {
        if (active) {
            textItem?.setIcon(R.drawable.ic_fluent_text_field_24_filled)
            textItem?.title = getString(R.string.action_text_off)
            view?.findViewById<ScrollView>(R.id.text_mode)?.bringToFront()
        } else {
            textItem?.setIcon(R.drawable.ic_fluent_text_field_24_regular)
            textItem?.title = getString(R.string.action_text_on)
        }
    }

    fun activateImage(active: Boolean) {
        val imageTools = view?.findViewById<LinearLayout>(R.id.image_tools)
        if (active) {
            imageItem?.setIcon(R.drawable.ic_fluent_image_24_filled)
            imageItem?.title = getString(R.string.action_image_off)
            imageTools?.visibility = View.VISIBLE
            imageContainer.bringToFront()
        } else {
            imageItem?.setIcon(R.drawable.ic_fluent_image_24_regular)
            imageItem?.title = getString(R.string.action_image_on)
            imageTools?.visibility = View.INVISIBLE
            toggleButtonColor(view?.findViewById(R.id.delete_image), false)
            toggleDeleteImageMode(false)
        }
    }

    fun activateInk(active: Boolean) {
        val penTools = view?.findViewById<LinearLayout>(R.id.pen_tools)
        if (active) {
            inkItem?.setIcon(R.drawable.ic_fluent_inking_tool_24_filled)
            inkItem?.title = getString(R.string.action_ink_off)
            drawView.enable()
            view?.findViewById<ConstraintLayout>(R.id.ink_mode)?.bringToFront()
            penTools?.visibility = View.VISIBLE
            penTools?.bringToFront()
        } else {
            inkItem?.setIcon(R.drawable.ic_fluent_inking_tool_24_regular)
            inkItem?.title = getString(R.string.action_ink_on)
            drawView.disable()
            // Close pen tools and reset button states
            penTools?.visibility = View.INVISIBLE
            toggleViewVisibility(view?.findViewById<SeekBar>(R.id.thickness_slider), true)
            toggleViewVisibility(view?.findViewById<LinearLayout>(R.id.color_buttons), true)
            toggleButtonColor(view?.findViewById(R.id.thickness), false)
            toggleButtonColor(view?.findViewById(R.id.color), false)
            toggleButtonColor(view?.findViewById(R.id.highlight), drawView.toggleHighlightMode(false))
            toggleButtonColor(view?.findViewById(R.id.erase), drawView.toggleEraserMode(false))
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
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", file))
        startActivity(Intent.createChooser(intent, resources.getString(R.string.share_intent)))
    }

    /**
     * Close NoteDetailFragment after deletion and open either the NoteListFragment (unspanned)
     * or the GetStartedFragment (spanned)
     */
    fun closeFragment() {
        activity?.let { activity ->
            if (ScreenHelper.isDualMode(activity) && !MainActivity.isRotated(activity)) {
                // Tell NoteListFragment that list data has changed
                (parentFragmentManager.findFragmentByTag(LIST_FRAGMENT) as? NoteListFragment)
                    ?.updateArrayAdapter()

                // If spanned and not rotated (list/detail view), show GetStartedFragment in second container
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.second_container_id,
                        GetStartedFragment(),
                        null
                    ).commit()
            } else {
                // If unspanned, or spanned and rotated (extended view), show NoteListFragment in first container
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.first_container_id,
                        NoteListFragment(),
                        LIST_FRAGMENT
                    ).commit()
            }
        }
    }

    // create drop targets for the editor screen
    private fun initializeDragListener() {
        dragHandler = DragHandler(this)

        // Main target will trigger when textField has content
        noteText.setOnDragListener { _, event ->
            dragHandler.onDrag(event)
        }

        // Sub target will trigger when textField is empty
        rootDetailLayout.setOnDragListener { _, event ->
            dragHandler.onDrag(event)
        }
    }
}
