/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.includes

import Defines.MIN_DIMEN
import Defines.RENDER_TIMER
import Defines.RESIZE_SPEED
import Defines.THRESHOLD
import android.annotation.SuppressLint
import android.content.ClipData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.util.Base64
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.drawToBitmap
import com.microsoft.device.display.samples.twonote.NoteDetailFragment
import com.microsoft.device.display.samples.twonote.SerializedImage
import java.io.ByteArrayOutputStream
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class ImageHandler(private val fragment: NoteDetailFragment) {
    private val names: MutableList<String> = mutableListOf()
    private val images: MutableList<ImageView> = mutableListOf()
    private val compressedImages: MutableList<String?> = mutableListOf()
    private var space = 0f
    private var prevHeight = 0
    private var prevWidth = 0
    private var clickStartTime = 0L

    // Create a new ImageView and add it to the container
    fun addImageToView(uri: Uri) {
        uri.lastPathSegment?.let { seg ->

            val imageView = ImageView(fragment.requireContext())
            imageView.id = View.generateViewId()
            imageView.setImageURI(uri)

            fragment.view?.let {
                fragment.imageContainer.addView(imageView)
            }

            createShadowDragListener(imageView)

            Handler().postDelayed(
                {
                    trackImageData(seg, imageView, getStringImage(imageView.drawToBitmap()))
                },
                RENDER_TIMER
            )
        }
    }

    private fun addImageToView(serialized: SerializedImage) {
        // Create a new ImageView and add it to the container
        val imageView = ImageView(fragment.requireContext())
        imageView.id = View.generateViewId()
        imageView.setImageBitmap(getImageString(serialized.image))

        imageView.x = serialized.coords[0]
        imageView.y = serialized.coords[1]
        imageView.layoutParams = RelativeLayout.LayoutParams(serialized.dimens[0], serialized.dimens[1])

        fragment.view?.let {
            trackImageData(serialized.name, imageView, serialized.image)
            fragment.imageContainer.addView(imageView)
        }
        createShadowDragListener(imageView)
    }

    private fun trackImageData(seg: String, imageView: ImageView, compressed: String?) {
        if (names.contains(seg)) {
            compressedImages.add("")
        } else {
            compressedImages.add(compressed)
        }
        images.add(imageView)
        names.add(seg)
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

    fun handleReposition(event: DragEvent): Boolean {
        return when (event.action) {
            DragEvent.ACTION_DROP -> {
                val view: View? = event.localState as View?
                view?.let { v ->
                    v.x = event.x - (v.width / 2)
                    v.y = event.y - (v.height / 2)
                    v.visibility = View.VISIBLE
                    v.bringToFront()
                }
                true
            }
            DragEvent.ACTION_DRAG_STARTED, DragEvent.ACTION_DRAG_ENDED -> true
            else -> false
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
        if (percentage < THRESHOLD && percentage > -THRESHOLD) percentage = 0f

        prevHeight = max((prevHeight + (RESIZE_SPEED * percentage)).toInt(), MIN_DIMEN)
        prevWidth = max((prevWidth + (RESIZE_SPEED * percentage)).toInt(), MIN_DIMEN)

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

    fun getImageList(): List<SerializedImage> {
        val list: MutableList<SerializedImage> = mutableListOf()

        for (index in images.indices) {
            if (images[index].visibility == View.VISIBLE) {
                compressedImages[names.indexOfFirst { it == names[index] }]?.let { image ->
                    list.add(
                        SerializedImage(
                            names[index],
                            image,
                            getImageCoords(index),
                            getImageDimen(index)
                        )
                    )
                }
            }
        }
        return list.toList()
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

    fun setImageList(list: List<SerializedImage>) {
        for (serialized in list) {
            addImageToView(serialized)
        }
    }

    private fun getStringImage(bitmap: Bitmap?): String? {
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes: ByteArray = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun getImageString(str: String): Bitmap? {
        val bytes = Base64.decode(str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}