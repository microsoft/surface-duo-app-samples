/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.includes

import Defines
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ContentResolver
import android.net.Uri
import android.net.Uri.fromFile
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.microsoft.device.display.samples.twonote.NoteDetailFragment
import com.microsoft.device.display.samples.twonote.SerializedImage
import java.io.File
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/* Class used to update app contents when an appropriate drag event occurs */
class DragHandler(val fragment: NoteDetailFragment) {

    private val fileHandler = FileHandler(fragment.requireActivity())
    private val images: MutableList<ImageView> = mutableListOf()
    private val uris: MutableList<Uri> = mutableListOf()
    private var space = 0f
    private var prevHeight = 0
    private var prevWidth = 0
    private var clickStartTime = 0L

    // drag occurred, decide if event is relevant to app
    fun onDrag(event: DragEvent): Boolean {
        val isText = event.clipDescription?.getMimeType(0)
            .toString().startsWith(Defines.TEXT_PREFIX)
        val isImage = event.clipDescription?.getMimeType(0)
            .toString().startsWith(Defines.IMAGE_PREFIX)

        return when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> true

            DragEvent.ACTION_DROP -> processDrop(event, isText, isImage)

            DragEvent.ACTION_DRAG_ENTERED, DragEvent.ACTION_DRAG_LOCATION,
            DragEvent.ACTION_DRAG_ENDED, DragEvent.ACTION_DRAG_EXITED ->
                // Ignore events
                true

            else -> false
        }
    }

    private fun processDrop(event: DragEvent, isText: Boolean, isImage: Boolean): Boolean {
        val item: ClipData.Item? = event.clipData?.getItemAt(0)

        item?.let {
            val uri = item.uri

            if (uri != null) {
                if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                    // Request permission to read file if it is outside the scope of the project
                    ActivityCompat.requestDragAndDropPermissions(fragment.activity, event)
                }
                if (isText) {
                    fileHandler.processTextFileData(uri, fragment.noteText)
                    return true
                } else if (isImage) {
                    addImageToView(uri)
                    return true
                } else {
                    // Dropped item type not supported
                    return false
                }
            } else {
                // Item from inside the app was dragged and dropped
                return handleReposition(event)
            }
        }
        return false
    }

    private fun addImageToView(uri: Uri): ImageView {
        // Define our ImageView and add it to layout
        val imageView = ImageView(fragment.requireContext())
        imageView.id = View.generateViewId()
        fileHandler.processImageData(uri, imageView)

        fragment.view?.let {
            images.add(imageView)
            uris.add(uri)
            fragment.imageContainer.addView(imageView)
        }

        createShadowDragListener(imageView)
        return imageView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createShadowDragListener(imageView: ImageView) {
        imageView.setOnTouchListener { v, e ->
            when (e.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // Initialize image resize
                    initResize(e, imageView)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (e.pointerCount > 1) {
                        // Resize image on pinch gesture
                        handleResize(e, imageView)
                    } else {
                        // Start dragging image on long click gesture
                        handleLongClick(v)
                    }
                    true
                }
                MotionEvent.ACTION_DOWN -> {
                    // Start long click timer
                    clickStartTime = Calendar.getInstance().timeInMillis
                    true
                }
                else -> true
            }
        }
    }

    private fun handleReposition(event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                val view: View? = event.localState as View?
                view?.let { v ->
                    v.x = event.x - (v.width / 2)
                    v.y = event.y - (v.height / 2)
                    v.visibility = View.VISIBLE
                    v.bringToFront()
                }

                return true
            }
            DragEvent.ACTION_DRAG_STARTED, DragEvent.ACTION_DRAG_ENDED -> return true
            else -> return false
        }
    }

    private fun initResize(e: MotionEvent, imageView: ImageView) {
        space = spacing(e)
        prevHeight = imageView.height
        prevWidth = imageView.width
        imageView.layoutParams = RelativeLayout.LayoutParams(prevWidth, prevHeight)
    }

    private fun handleResize(e: MotionEvent, imageView: ImageView) {
        clickStartTime = 0

        // formula reasoning    -> "spread out" will result in positive percentage (add pixels to dimen)
        //                      -> "pinch in" will result in negative percentage (sub pixels from dimen)
        var percentage: Float = (spacing(e) / space) - 1
        if (percentage < Defines.THRESHOLD && percentage > -Defines.THRESHOLD) percentage = 0f

        prevHeight = max((prevHeight + (Defines.RESIZE_SPEED * percentage)).toInt(), Defines.MIN_DIMEN)
        prevWidth = max((prevWidth + (Defines.RESIZE_SPEED * percentage)).toInt(), Defines.MIN_DIMEN)

        imageView.layoutParams = RelativeLayout.LayoutParams(prevWidth, prevHeight)
    }

    private fun handleLongClick(v: View) {
        val clickDuration = Calendar.getInstance().timeInMillis - clickStartTime
        if (clickStartTime > 0 && clickDuration >= ViewConfiguration.getLongPressTimeout()) {
            val data = ClipData.newPlainText("", "")
            val shadowBuilder = View.DragShadowBuilder(v)
            v.startDragAndDrop(data, shadowBuilder, v, 0)
            v.visibility = View.INVISIBLE
        }
    }

    // returns the distance between two points on the screen
    private fun spacing(e: MotionEvent): Float {
        val pointer0 = MotionEvent.PointerCoords()
        e.getPointerCoords(0, pointer0)

        val pointer1 = MotionEvent.PointerCoords()
        e.getPointerCoords(1, pointer1)

        val xDist = abs(pointer0.x - pointer1.x)
        val yDist = abs(pointer0.y - pointer1.y)

        return sqrt((xDist * xDist) + (yDist * yDist))
    }

    private fun getImageCoords(index: Int): List<Float> {
        val list: MutableList<Float> = mutableListOf()

        list.add(images[index].x)
        list.add(images[index].y)

        return list.toList()
    }

    private fun getImageDimen(index: Int): List<Int> {
        val list: MutableList<Int> = mutableListOf()

        list.add(images[index].layoutParams.width)
        list.add(images[index].layoutParams.height)

        return list.toList()
    }

    fun getImageList(): List<SerializedImage> {
        val list: MutableList<SerializedImage> = mutableListOf()

        if (images.size != uris.size)
            return list.toList()

        for (index in images.indices) {
            if (images[index].visibility == View.VISIBLE) {
                uris[index].authority?.let { authority ->
                    uris[index].path?.let { path ->
                        list.add(
                            SerializedImage(
                                authority,
                                path,
                                getImageCoords(index),
                                getImageDimen(index)
                            )
                        )
                    }
                }
            }
        }
        return list.toList()
    }

    fun setImageList(list: List<SerializedImage>) {
        for (serialized in list) {
            fromFile(File(serialized.path))?.let {
                val permissions: MutableList<String> = mutableListOf()
                permissions.add(android.Manifest.permission.ACCESS_MEDIA_LOCATION)
                ActivityCompat.requestPermissions(fragment.requireActivity(), permissions.toTypedArray(), 100)
                val image = addImageToView(it)
                //image.layoutParams = RelativeLayout.LayoutParams(serialized.dimens[0], serialized.dimens[1])
                //image.x = serialized.coords[0]
                //image.y = serialized.coords[1]
            }
        }
    }
}