/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.includes

import Defines.LAND_TO_PORT
import Defines.MIN_DIMEN
import Defines.PORT_TO_LAND
import Defines.RENDER_TIMER
import Defines.RESIZE_SPEED
import Defines.SCALE_RATIO
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
import com.microsoft.device.display.samples.twonote.MainActivity
import com.microsoft.device.display.samples.twonote.NoteDetailFragment
import com.microsoft.device.display.samples.twonote.structures.SerializedImage
import java.io.ByteArrayOutputStream
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ImageHandler(private val fragment: NoteDetailFragment) {
    private val names: MutableList<String> = mutableListOf()
    private val images: MutableList<ImageView> = mutableListOf()
    private val compressedImages: MutableList<String?> = mutableListOf()
    private val rotations: MutableList<Boolean> = mutableListOf()
    private var space = 0f
    private var prevHeight = 0
    private var prevWidth = 0
    private var clickStartTime = 0L
    private var deleteMode = false

    // Create a new ImageView and add it to the container
    fun addImageToView(uri: Uri, isRotated: Boolean) {
        uri.lastPathSegment?.let { seg ->

            val imageView = ImageView(fragment.requireContext())
            imageView.id = View.generateViewId()
            imageView.setImageURI(uri)

            createShadowDragListener(imageView)

            Handler().postDelayed(
                {
                    trackImageData(seg, imageView, encodeImage(imageView.drawToBitmap()), isRotated)
                },
                RENDER_TIMER
            )
        }
    }

    private fun addImageToView(serialized: SerializedImage, isRotated: Boolean) {
        // Create a new ImageView and add it to the container
        val imageView = ImageView(fragment.requireContext())
        imageView.id = View.generateViewId()
        imageView.setImageBitmap(decodeImage(serialized.image))

        // Extract serialized image properties
        val coords = serialized.coords.toFloatArray()
        var w = serialized.dimens[0].toFloat()
        var h = serialized.dimens[1].toFloat()

        // Check if current rotation matches the rotation of the serialized image
        if (isRotated != serialized.rotated) {
            when (isRotated) {
                true -> {
                    // If currently rotated, rotate from portrait to landscape
                    PORT_TO_LAND.mapPoints(coords, serialized.coords.toFloatArray())
                    w *= SCALE_RATIO
                    h *= SCALE_RATIO
                }
                false -> {
                    // If not currently rotated, rotate from landscape to portrait
                    LAND_TO_PORT.mapPoints(coords, serialized.coords.toFloatArray())
                    w /= SCALE_RATIO
                    h /= SCALE_RATIO
                }
            }
        }

        // Set image view properties based on processed serialized image properties
        // Note: small loss of precision in rotation scaling when rounding the width/height from float to int
        imageView.x = coords[0]
        imageView.y = coords[1]
        imageView.layoutParams = RelativeLayout.LayoutParams(w.roundToInt(), h.roundToInt())

        fragment.view?.let {
            // Update serialized image's rotation to match current rotation after scaling
            trackImageData(serialized.name, imageView, serialized.image, isRotated)
            fragment.imageContainer.addView(imageView)
        }
        createShadowDragListener(imageView)
    }

    /**
     * When a new image is added, add its data to the ImageHandler's lists
     */
    private fun trackImageData(seg: String, imageView: ImageView, compressed: String?, isRotated: Boolean) {
        if (names.contains(seg)) {
            // Add empty string to keep array indices aligned
            compressedImages.add("")
        } else {
            compressedImages.add(compressed)
        }
        images.add(imageView)
        names.add(seg)
        rotations.add(isRotated)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createShadowDragListener(imageView: ImageView) {
        imageView.setOnTouchListener { v, e ->
            when (e.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // Initialize image resize
                    initResize(e, imageView, MainActivity.isRotated(fragment.requireContext()))
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
                    if (deleteMode) {
                        deleteImage(v)
                    } else {
                        // Start long click timer
                        clickStartTime = Calendar.getInstance().timeInMillis
                    }
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

    private fun initResize(e: MotionEvent, imageView: ImageView, isRotated: Boolean) {
        space = spacing(e)
        prevHeight = imageView.height
        prevWidth = imageView.width
        imageView.layoutParams = RelativeLayout.LayoutParams(prevWidth, prevHeight)

        // Update rotation value for the image that's about to be resized
        if (images.contains(imageView)) {
            val index = images.indexOf(imageView)
            rotations[index] = isRotated
        }
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
            // On long click, make a shadow of the image to drag around
            val data = ClipData.newPlainText("", "")
            val shadowBuilder = View.DragShadowBuilder(v)
            v.startDragAndDrop(data, shadowBuilder, v, 0)
            v.visibility = View.INVISIBLE
        }
    }

    private fun deleteImage(v: View) {
        val imageView = v as? View ?: return

        // Remove ImageView from view
        fragment.view?.let {
            fragment.imageContainer.removeView(v)
        }

        // Remove image data from lists
        val index = images.indexOf(imageView)
        images.removeAt(index)
        rotations.removeAt(index)
        names.removeAt(index)
        compressedImages.removeAt(index)
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
                            getImageDimen(index),
                            rotations[index]
                        )
                    )
                }
            }
        }
        return list.toList()
    }

    fun getImageViewList(): List<ImageView> {
        return images
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

    fun setImageList(list: List<SerializedImage>, isRotated: Boolean) {
        // Clear the existing image data from the lists
        names.clear()
        compressedImages.clear()
        rotations.clear()
        images.clear()

        for (serialized in list) {
            addImageToView(serialized, isRotated)
        }
    }

    fun setDeleteMode(value: Boolean) {
        deleteMode = value
    }

    private fun encodeImage(bitmap: Bitmap?): String? {
        val stream = ByteArrayOutputStream()
        // compression using 100% image quality
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes: ByteArray = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun decodeImage(str: String): Bitmap? {
        val bytes = Base64.decode(str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}