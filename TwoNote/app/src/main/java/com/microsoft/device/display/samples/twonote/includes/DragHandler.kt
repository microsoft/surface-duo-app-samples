/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.includes

import android.app.Activity
import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.device.display.samples.twonote.NoteDetailFragment

/* Class used to update app contents when an appropriate drag event occurs */
class DragHandler(val fragment: NoteDetailFragment) {

    private val fileHandler = FileHandler(fragment.requireActivity())

    // drag occurred, decide if event is relevant to app
    fun onDrag(event: DragEvent): Boolean {
        val action = event.action
        val isText = event.clipDescription?.getMimeType(0)
            .toString().startsWith(Defines.TEXT_PREFIX)
        val isImage = event.clipDescription?.getMimeType(0)
            .toString().startsWith(Defines.IMAGE_PREFIX)

        return when (action) {
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
        val item: ClipData.Item = event.clipData.getItemAt(0)
        val uri = item.uri

        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            ActivityCompat.requestDragAndDropPermissions(fragment.activity, event)
        }

        if (isText) {
            fileHandler.processTextFileData(uri, fragment.noteText, event)
        } else if (isImage) {
            fileHandler.processImageData(uri, fragment.picturePicture)

            fragment.view?.let {
                fragment.picturePicture.y = (event.y - (it.height / 2))
                fragment.picturePicture.x = (event.x - (it.width / 2))
            }
        }
        return true
    }
}