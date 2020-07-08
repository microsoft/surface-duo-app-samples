/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.sourceeditor.includes

import android.app.Activity
import android.content.ContentResolver
import android.view.DragEvent
import android.view.View
import com.microsoft.device.display.samples.sourceeditor.viewmodel.WebViewModel

/* Class used to update app contents when an appropriate drag event occurs */
class DragHandler (val activity: Activity,
                   val webVM: WebViewModel,
                   val contentResolver: ContentResolver) {

    val fileHandler = FileHandler(activity, webVM, contentResolver)

    // drag occurred, decide if event is relevant to app
    fun onDrag(v: View, event: DragEvent): Boolean {
        val action = event.action
        val isText = event.clipDescription?.getMimeType(0)
                        .toString().startsWith(Defines.TEXT_PREFIX)

        when (action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (isText) {
                    return true
                }
                return false
            }

            DragEvent.ACTION_DROP -> {
                if (isText) {
                    handleTextDrop(event)
                }
                return true
            }

            DragEvent.ACTION_DRAG_ENTERED, DragEvent.ACTION_DRAG_LOCATION,
                DragEvent.ACTION_DRAG_ENDED, DragEvent.ACTION_DRAG_EXITED ->
                // Ignore events
                return true

            else -> {
            }
        }
        return false
    }

    // process the file that was dropped
    private fun handleTextDrop(event: DragEvent) {
        val item = event.clipData.getItemAt(0)
        val uri = item.uri
        fileHandler.processFileData(uri)
    }
}