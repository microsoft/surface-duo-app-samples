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
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import com.microsoft.device.display.samples.twonote.NoteDetailFragment
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/* Class used to update app contents when an appropriate drag event occurs */
class DragHandler(val fragment: NoteDetailFragment) {

    private val fileHandler = FileHandler(fragment.requireActivity())
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

        return when (event.action and MotionEvent.ACTION_MASK) {
            DragEvent.ACTION_DRAG_STARTED -> true

            DragEvent.ACTION_DROP -> processDrop(event, isText, isImage)

            DragEvent.ACTION_DRAG_ENTERED, DragEvent.ACTION_DRAG_LOCATION,
            DragEvent.ACTION_DRAG_ENDED, DragEvent.ACTION_DRAG_EXITED ->
                // Ignore events
                true

            else -> false
        }
    }

    private fun processDrop(event: DragEvent, isText: Boolean, isImage: Boolean) : Boolean {
        val item: ClipData.Item? = event.clipData?.getItemAt(0)

        item?.let {
            val uri = item.uri

            if (uri != null) {
                if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                    ActivityCompat.requestDragAndDropPermissions(fragment.activity, event)
                }
                if (isText) {
                    fileHandler.processTextFileData(uri, fragment.noteText, event)
                    return true
                } else if (isImage) {
                    addImageToView(uri)
                    return true
                } else {
                    return false
                }
            } else {
                return handleReposition(event)
            }
        }
        return false
    }

    private fun addImageToView(uri: Uri) {
        // Define our ImageView and add it to layout
        val imageView = ImageView(fragment.requireContext())
        imageView.id = View.generateViewId()
        fileHandler.processImageData(uri, imageView)

        fragment.view?.let {
            imageView.adjustViewBounds = true
            fragment.imageContainer.addView(imageView)
            fragment.imageContainer.bringToFront()
            imageView.bringToFront()
        }

        createShadowDragListener(imageView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createShadowDragListener(imageView: ImageView) {
        imageView.setOnTouchListener { v, e ->
            when (e.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    space = spacing(e)
                    prevHeight = imageView.height
                    prevWidth = imageView.width
                    imageView.layoutParams = RelativeLayout.LayoutParams(prevWidth, prevHeight)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (e.pointerCount > 1) {
                        clickStartTime = 0

                        var percentage: Float = (spacing(e) / space) - 1
                        if (percentage < 0.2 && percentage > -0.2) percentage = 0f

                        prevHeight = max((prevHeight + (15 * percentage)).toInt(),250)
                        prevWidth = max((prevWidth + (15 * percentage)).toInt(),250)

                        imageView.layoutParams = RelativeLayout.LayoutParams(prevWidth, prevHeight)

                        Log.d("IMGDRGGNG", "${imageView.maxHeight}")
                    } else {
                        val clickDuration = Calendar.getInstance().timeInMillis - clickStartTime
                        if (clickStartTime > 0 && clickDuration >= ViewConfiguration.getLongPressTimeout()) {
                            val data = ClipData.newPlainText("", "")
                            val shadowBuilder = View.DragShadowBuilder(v)
                            v.startDragAndDrop(data, shadowBuilder, v, 0)
                            v.visibility = View.INVISIBLE
                        }
                    }
                    true
                }
                MotionEvent.ACTION_DOWN -> {
                    clickStartTime = Calendar.getInstance().timeInMillis
                    true
                }/*MotionEvent.ACTION_DOWN -> {
                    val data = ClipData.newPlainText("", "")
                    val shadowBuilder = DragShadowBuilder(v)
                    v.startDragAndDrop(data, shadowBuilder, v, 0)
                    v.visibility = View.INVISIBLE
                    true
                }*/
                else -> true
            }
        }
    }

    private fun handleReposition(event: DragEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            DragEvent.ACTION_DROP -> {
                val view: View? = event.localState as View?
                //val owner = view?.parent as ViewGroup?
                //owner?.removeView(view)
                //val container = fragment.imageContainer
                //container.addView(view)
                view?.let {v ->
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

    private fun spacing(e: MotionEvent): Float {
        val pointer0 = MotionEvent.PointerCoords()
        e.getPointerCoords(0, pointer0)

        val pointer1 = MotionEvent.PointerCoords()
        e.getPointerCoords(1, pointer1)

        val xDist = abs(pointer0.x - pointer1.x)
        val yDist = abs(pointer0.y - pointer1.y)
        return sqrt((xDist*xDist) + (yDist*yDist))
    }
}