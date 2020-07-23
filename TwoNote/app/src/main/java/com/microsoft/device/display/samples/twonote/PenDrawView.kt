/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.lang.Math.max
import kotlin.math.min

class PenDrawView : View {
    private var currentColor: Int = 0
    private var drawBitmap: Bitmap? = null
    private var strokeList: MutableList<Stroke> = mutableListOf()
    private var radius = 0
    private var isErasing = false
    private val eraser = RectF()
    private var prevPressure = 0f
    private var disabled = true

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
            for (line in strokeList.size - 1 downTo 0) {
                val stroke = strokeList[line]
                val pathList = stroke.getPathList()
                val paints = stroke.getPaints()
                val bounds = stroke.getBounds()

                if (pathList.isNotEmpty()) {
                    for (section in pathList.size - 1 downTo 0) {
                        val bound = bounds[section]
                        val path = pathList[section]
                        val paint = paints[section]

                        if (isErasing && bound.intersects(eraser.left, eraser.top, eraser.right, eraser.bottom)) {
                            val newStroke = stroke.removeItem(section)
                            newStroke?.let { strokeList.add(newStroke) }
                            if (stroke.getSize() == 0)
                                strokeList.removeAt(line)
                        } else {
                            val configuredPaint = configurePaint(paint)
                            canvas.drawPath(path, configuredPaint)
                        }
                    }
                }
            }
        }
    }

    fun disable() {
        disabled = true
    }

    fun enable() {
        disabled = false
    }

    fun isDisabled(): Boolean {
        return disabled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isDisabled())
            return true

        isErasing = false
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER) {
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
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val stroke = Stroke(event.x, event.y, event.pressure, currentColor)
                    strokeList.add(stroke)
                    prevPressure = event.pressure
                }

                MotionEvent.ACTION_MOVE -> {
                    Log.d("draw_debugging", "pressure " + event.pressure)
                    if (strokeList.isNotEmpty())
                        strokeList[strokeList.lastIndex].continueDrawing(event.x, event.y, event.pressure)
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

    private fun configurePaint(paint: Paint): Paint {
        val configuredPaint = Paint()

        configuredPaint.style = Paint.Style.STROKE
        configuredPaint.isAntiAlias = true
        configuredPaint.strokeCap = Paint.Cap.ROUND
        configuredPaint.strokeWidth = paint.strokeWidth + radius / 3
        configuredPaint.color = paint.color + radius / 4
        return configuredPaint
    }

    fun setPaintRadius(radius: Int) {
        this.radius = radius
        this.invalidate()
    }

    fun getStrokeList(): List<Stroke> {
        return strokeList
    }

    fun setStrokeList(s: List<Stroke>) {
        strokeList = s.toMutableList()
    }

    fun getDataList(): List<SerializedStroke> {
        val list: MutableList<SerializedStroke> = mutableListOf()
        for (stroke in strokeList) {
            list.add(stroke.serializeData())
        }
        return list.toList()
    }

    fun changePaintColor(color: Int) {
        currentColor = color
    }

    fun clearDrawing() {
        this.drawBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        strokeList.clear()

        invalidate()
    }

    private fun takeScreenshotOfView(view: View, height: Int, width: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }
}
