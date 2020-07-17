/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import java.io.Serializable

// does this have to be serializable?
class Stroke : Serializable {

    private var xList: MutableList<MutableList<Float>> = mutableListOf()
    private var yList: MutableList<MutableList<Float>> = mutableListOf()
    private var pressureList: MutableList<MutableList<Float>> = mutableListOf()
    private var paintColor: Int = 0

    private var pathList: MutableList<Path> = mutableListOf()
    private var paints: MutableList<Paint> = mutableListOf()
    private var pathBounds: MutableList<RectF> = mutableListOf()

    private var xCoord: Float = 0f
    private var yCoord: Float = 0f
    private var prevPressure: Float = 0f

    constructor(x: Float, y: Float, pressure: Float, color: Int) {
        initStroke(x, y, pressure, color)
    }

    // reconstruct serialized data
    constructor(
        x: List<MutableList<Float>>,
        y: List<MutableList<Float>>,
        pressure: List<MutableList<Float>>,
        color: Int
    ) {
        // need at least one path
        if (x.size > 0 && x[0].size > 0) {
            for (paths in 0 until x.size) {
                for (coords in 0 until x[paths].size) {
                    if (paths == 0 && coords == 0)
                        initStroke(x[paths][coords], y[paths][coords], pressure[paths][coords], color)
                    else
                        continueDrawing(x[paths][coords], y[paths][coords], pressure[paths][coords])
                }
            }
            finishStroke()
        }
    }

    fun continueDrawing(x: Float, y: Float, pressure: Float) {
        if (pressure == prevPressure && !pathList.isEmpty())
            continueJoint(x, y)
        else
            addJoint(x, y, pressure)
    }

    fun finishStroke() {
        if (!pathList.isEmpty()) {
            val bounds = RectF()
            pathList[pathList.lastIndex].computeBounds(bounds, true)
            pathBounds.removeAt(pathBounds.lastIndex)
            pathBounds.add(bounds)
        }
    }

    private fun addJoint(x: Float, y: Float, pressure: Float) {
        finishStroke()
        initPath()

        val paint = Paint()
        paint.color = paintColor
        paint.strokeWidth = pressure * 25

        val path = Path()
        path.moveTo(xCoord, yCoord)
        path.lineTo(x, y)
        pathList.add(path)
        pathBounds.add(RectF())
        paints.add(paint)

        updateValues(x, y, pressure)
    }

    private fun continueJoint(x: Float, y: Float) {
        if (!pathList.isEmpty()) {
            pathList[pathList.lastIndex].lineTo(x, y)
            updateValues(x, y, prevPressure)
        }
    }

    fun getBounds(): MutableList<RectF> {
        return pathBounds
    }

    fun getPathList(): MutableList<Path> {
        return pathList
    }

    fun getPaints(): MutableList<Paint> {
        return paints
    }

    // returns newly created stroke if applicable
    fun removeItem(i: Int): Stroke? {
        var stroke: Stroke? = null

        // split the stroke into two if middle is erased
        if (i > 0 && i < xList.lastIndex) {
            stroke = splitStroke(i)
        }

        deleteComponents(i)
        return stroke
    }

    private fun splitStroke(i: Int): Stroke {
        var range = IntRange(i + 1, xList.lastIndex)
        val x: List<MutableList<Float>> = xList.slice(range)
        val y: List<MutableList<Float>> = yList.slice(range)
        val p: List<MutableList<Float>> = pressureList.slice(range)

        val stroke = Stroke(x, y, p, paintColor)

        range = IntRange(0, i)
        xList = xList.slice(range).toMutableList()
        yList = yList.slice(range).toMutableList()
        pressureList = pressureList.slice(range).toMutableList()
        pathBounds = pathBounds.slice(range).toMutableList()
        pathList = pathList.slice(range).toMutableList()
        paints = paints.slice(range).toMutableList()

        return stroke
    }

    private fun deleteComponents(i: Int) {
        xList.removeAt(i)
        yList.removeAt(i)
        pressureList.removeAt(i)
        pathBounds.removeAt(i)
        pathList.removeAt(i)
        paints.removeAt(i)
    }

    fun getSize(): Int {
        return pathList.size
    }

    private fun updateValues(x: Float, y: Float, pressure: Float) {
        xCoord = x
        yCoord = y
        prevPressure = pressure

        xList[xList.lastIndex].add(x)
        yList[yList.lastIndex].add(y)
        pressureList[pressureList.lastIndex].add(pressure)
    }

    private fun initStroke(x: Float, y: Float, pressure: Float, color: Int) {
        xCoord = x
        yCoord = y
        prevPressure = pressure
        paintColor = color

        addJoint(x, y, pressure)
    }

    private fun initPath() {
        xList.add(mutableListOf())
        yList.add(mutableListOf())
        pressureList.add(mutableListOf())
    }

    fun serializeData(): SerializedStroke {
        return SerializedStroke(xList, yList, pressureList, paintColor)
    }
}
