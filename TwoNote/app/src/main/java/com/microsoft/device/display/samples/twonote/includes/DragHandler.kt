/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.includes

import Defines.IMAGE_PREFIX
import Defines.TEXT_PREFIX
import android.content.ClipData
import android.content.ContentResolver
import android.view.DragEvent
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.microsoft.device.display.samples.twonote.MainActivity
import com.microsoft.device.display.samples.twonote.NoteDetailFragment
import com.microsoft.device.display.samples.twonote.structures.SerializedImage

/* Class used to update app contents when an appropriate drag event occurs */
class DragHandler(private val fragment: NoteDetailFragment) {

    private val fileHandler = FileHandler(fragment.requireActivity())
    private val imageHandler = ImageHandler(fragment)

    // drag occurred, decide if event is relevant to app
    fun onDrag(event: DragEvent): Boolean {
        val isText = event.clipDescription?.getMimeType(0)
            .toString().startsWith(TEXT_PREFIX)
        val isImage = event.clipDescription?.getMimeType(0)
            .toString().startsWith(IMAGE_PREFIX)
        val isRotated = MainActivity.isRotated(fragment.requireContext())

        return when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> true

            DragEvent.ACTION_DROP -> processDrop(event, isText, isImage, isRotated)

            DragEvent.ACTION_DRAG_ENTERED, DragEvent.ACTION_DRAG_LOCATION,
            DragEvent.ACTION_DRAG_ENDED, DragEvent.ACTION_DRAG_EXITED ->
                // Ignore events
                true

            else -> false
        }
    }

    private fun processDrop(event: DragEvent, isText: Boolean, isImage: Boolean, isRotated: Boolean): Boolean {
        val item: ClipData.Item? = event.clipData?.getItemAt(0)

        item?.let {
            val uri = item.uri

            if (uri != null) {
                if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                    // Request permission to read file if it is outside the scope of the project
                    ActivityCompat.requestDragAndDropPermissions(fragment.activity, event)

                    when {
                        isText -> {
                            fileHandler.processTextFileData(uri, fragment.noteText)
                            fragment.activateText(true)
                            fragment.activateImage(false)
                            fragment.activateInk(false)
                            return true
                        }
                        isImage -> {
                            imageHandler.addImageToView(uri, isRotated)
                            fragment.activateText(false)
                            fragment.activateImage(true)
                            fragment.activateInk(false)
                            return true
                        }
                        else -> {
                            // Dropped item type not supported
                            return false
                        }
                    }
                }
            } else {
                // Item from inside the app was dragged and dropped
                return imageHandler.handleReposition(event)
            }
        }
        return false
    }

    fun setImageList(list: List<SerializedImage>, isRotated: Boolean) {
        imageHandler.setImageList(list, isRotated)
    }

    fun getImageList(): List<SerializedImage> {
        return imageHandler.getImageList()
    }

    fun getImageViewList(): List<ImageView> {
        return imageHandler.getImageViewList()
    }

    fun setDeleteMode(value: Boolean) {
        imageHandler.setDeleteMode(value)
    }
}