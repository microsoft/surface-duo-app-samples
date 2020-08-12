/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import Defines.LAND_TO_PORT
import Defines.PORT_TO_LAND
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import com.microsoft.device.display.samples.twonote.structures.SerializedStroke
import com.microsoft.device.display.samples.twonote.structures.Stroke
import java.lang.Math.max
import kotlin.math.min

class PenDrawView : View {
    private var currentColor: Int = 0
    private var strokeList: MutableList<Stroke> = mutableListOf()
    private var isErasing = false
    private val eraser = RectF()
    private var disabled = true
    private var currentThickness: Int = 25
    var rotated = false
    private var highlightMode = false
    private var eraserMode = false

    companion object {
        // Attributes used for scaling drawings based on rotation
        private var scaledPath = Path()
        private var scaledBound = RectF()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        currentColor = Color.RED
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (strokeList.isNotEmpty()) {
            var line = 0
            while (line < strokeList.size && line >= 0) {
                val stroke = strokeList[line]
                val pathList = stroke.getPathList()
                val paints = stroke.getPaints()
                val bounds = stroke.getBounds()

                if (pathList.isNotEmpty()) {
                    var section = 0
                    while (section < stroke.getSize() && section >= 0) {
                        val bound = bounds[section]
                        val path = pathList[section]
                        val paint = paints[section]
                        val diffRotations = rotated != stroke.getRotation()
                        val matrix = when (rotated) {
                            true -> PORT_TO_LAND
                            false -> LAND_TO_PORT
                        }

                        // If strokes were drawn in a different rotation state than is currently displayed,
                        // transform their paths and bounds to match the current rotation state
                        if (diffRotations) {
                            matrix.mapRect(scaledBound, bound)
                            path.transform(matrix, scaledPath)
                        }

                        // If a stroke or path is removed, decrease the index so the next stroke/path doesn't get skipped
                        if (isErasing && ((!diffRotations && bound.intersect(eraser)) || (diffRotations && scaledBound.intersect(eraser)))) {
                            val newStroke = stroke.removeItem(section)
                            section--
                            // Add split stroke to next position in stroke list to maintain chronological order of strokes for undo
                            newStroke?.let { strokeList.add(line + 1, newStroke) }
                            if (stroke.getSize() == 0) {
                                strokeList.removeAt(line)
                                line--
                            }
                        } else {
                            val configurePaint = configurePaint(paint, stroke.getHighlight())
                            val pathToDraw = if (diffRotations) scaledPath else path
                            canvas.drawPath(pathToDraw, configurePaint)
                        }
                        section++
                    }
                }
                line++
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (disabled)
            return true

        isErasing = false
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER || eraserMode) {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                isErasing = true

                val offset = 50f
                val left = max(event.x - offset, 0f)
                val right = min(event.x + offset, width.toFloat() - 1)
                val top = min(event.y - offset, height.toFloat() - 1)
                val bottom = max(event.y + offset, 0f)
                eraser.set(left, top, right, bottom)
            }
        } else {
            // Keep constant pressure if in highlight mode (1 = normal pressure)
            val pressure = if (highlightMode) 1f else event.pressure
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val stroke = Stroke(event.x, event.y, pressure, currentColor, currentThickness, rotated, highlightMode)
                    strokeList.add(stroke)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (strokeList.isNotEmpty())
                        strokeList[strokeList.lastIndex].continueDrawing(event.x, event.y, pressure)
                }
                MotionEvent.ACTION_UP -> {
                    if (strokeList.isNotEmpty())
                        strokeList[strokeList.lastIndex].finishStroke()
                }
            }
        }
        invalidate()

        return true
    }

    private fun configurePaint(paint: Paint, highlight: Boolean = false): Paint {
        val configuredPaint = Paint()

        configuredPaint.style = Paint.Style.STROKE
        configuredPaint.isAntiAlias = true
        configuredPaint.strokeCap = if (highlight) Paint.Cap.SQUARE else Paint.Cap.ROUND
        configuredPaint.strokeWidth = paint.strokeWidth
        configuredPaint.color = paint.color

        return configuredPaint
    }

    fun toggleHighlightMode(force: Boolean? = null): Boolean {
        highlightMode = force ?: !highlightMode
        changePaintColor(currentColor)
        return highlightMode
    }

    fun toggleEraserMode(force: Boolean? = null): Boolean {
        eraserMode = force ?: !eraserMode
        return eraserMode
    }

    fun setStrokeList(s: List<Stroke>) {
        strokeList = s.toMutableList()
    }

    fun getDrawingList(): List<SerializedStroke> {
        val list: MutableList<SerializedStroke> = mutableListOf()
        for (stroke in strokeList) {
            list.add(stroke.serializeData())
        }
        return list.toList()
    }

    fun changePaintColor(color: Int) {
        // TODO: adjust highlight for dark theme
        // alpha values range from 0 (transparent) to 255 (opaque)
        currentColor = if (highlightMode)
            ColorUtils.setAlphaComponent(color, 100)
        else
            ColorUtils.setAlphaComponent(color, 255)
    }

    fun changeThickness(thickness: Int) {
        currentThickness = thickness
    }

    fun clearDrawing() {
        strokeList.clear()
        invalidate()
    }

    fun undo() {
        if (strokeList.isNotEmpty()) {
            strokeList.removeAt(strokeList.lastIndex)
            invalidate()
        }
    }

    fun disable() {
        disabled = true
    }

    fun enable() {
        disabled = false
    }
}
