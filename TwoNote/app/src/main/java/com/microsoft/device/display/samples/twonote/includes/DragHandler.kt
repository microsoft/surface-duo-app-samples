/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.includes

import android.app.Activity
import android.content.ContentResolver
import android.view.DragEvent
import com.google.android.material.textfield.TextInputEditText

/* Class used to update app contents when an appropriate drag event occurs */
class DragHandler(activity: Activity, textField: TextInputEditText, contentResolver: ContentResolver) {

    private val fileHandler = FileHandler(activity, textField, contentResolver)

    // drag occurred, decide if event is relevant to app
    fun onDrag(event: DragEvent): Boolean {
        val action = event.action
        val isText = event.clipDescription?.getMimeType(0)
            .toString().startsWith(Defines.TEXT_PREFIX)

        return when (action) {
            DragEvent.ACTION_DRAG_STARTED -> isText

            DragEvent.ACTION_DROP -> {
                handleTextDrop(event)
                isText
            }

            DragEvent.ACTION_DRAG_ENTERED, DragEvent.ACTION_DRAG_LOCATION,
            DragEvent.ACTION_DRAG_ENDED, DragEvent.ACTION_DRAG_EXITED ->
                // Ignore events
                return true

            else -> false
        }
    }

    // process the file that was dropped
    private fun handleTextDrop(event: DragEvent) {
        val item = event.clipData.getItemAt(0)
        val uri = item.uri
        fileHandler.processFileData(uri, event)
    }
}