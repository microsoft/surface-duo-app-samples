/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.includes

import Defines
import android.content.ClipData
import android.content.ContentResolver
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Guideline
import androidx.core.app.ActivityCompat
import com.microsoft.device.display.samples.twonote.NoteDetailFragment
import com.microsoft.device.display.samples.twonote.R
import kotlinx.android.synthetic.main.fragment_note_detail.view.*


/* Class used to update app contents when an appropriate drag event occurs */
class DragHandler(val fragment: NoteDetailFragment) {

    private val fileHandler = FileHandler(fragment.requireActivity())

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
                } else if (isImage) {
                    // Define our ImageView and add it to layout
                    val imageView = ImageView(fragment.requireContext())
                    imageView.id = View.generateViewId()
                    fileHandler.processImageData(uri, imageView)


                    // TODO: put imageview inside another container so that it doesn't have the size of rootview
                    fragment.view?.let {
                        imageView.adjustViewBounds = true
                        fragment.imageContainer.addView(imageView)
                        fragment.imageContainer.bringToFront()
                        imageView.bringToFront()
                    }

                    imageView.setOnTouchListener { v, e ->
                        when (e.action and MotionEvent.ACTION_MASK) {
                            MotionEvent.ACTION_DOWN -> {
                                val data = ClipData.newPlainText("", "")
                                val shadowBuilder = DragShadowBuilder(v)
                                v.startDragAndDrop(data, shadowBuilder, v, 0)
                                v.visibility = View.INVISIBLE
                                true
                            }
                            else -> false
                        }
                    }
                }
                else{}
            } else {
                when (event.action and MotionEvent.ACTION_MASK) {
                    DragEvent.ACTION_DROP -> {
                        val view: View? = event.localState as View?
                        val owner = view?.parent as ViewGroup?
                        owner?.removeView(view)
                        val container = fragment.rootDetailLayout
                        container.addView(view)
                        view?.let {v ->
                            v.x = event.x - (v.width / 2)
                            v.y = event.y - (v.height / 2)
                            v.visibility = View.VISIBLE
                        }

                        true
                    }
                    DragEvent.ACTION_DRAG_STARTED, DragEvent.ACTION_DRAG_ENDED -> true
                    else -> false
                }
            }
        }
        return false
    }
}