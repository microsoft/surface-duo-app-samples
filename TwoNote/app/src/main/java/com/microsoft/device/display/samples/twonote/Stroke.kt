package com.microsoft.device.display.samples.twonote

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import java.io.Serializable

class Stroke  : Serializable {

    private var xList: MutableList<Float> = mutableListOf()
    private var yList: MutableList<Float> = mutableListOf()
    private var pressureList: MutableList<Float> = mutableListOf()
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
            x: MutableList<Float>,
            y: MutableList<Float>,
            pressure: MutableList<Float>,
            color: Int
    ) {
        // need two points to make a line
        if (x.size > 2) {
            initStroke(x[0], y[0], pressure[0], color)
            for (coords in 1 until x.size) {
                continueDrawing(x[coords], y[coords], pressure[coords])
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

        val paint = Paint()
        paint.color = paintColor
        paint.strokeWidth = pressure * 25
        pressureList.add(pressure)

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

    // returns false if the stroke is now empty
    fun removeItem(i: Int): Boolean {
        pathBounds.removeAt(i)
        pathList.removeAt(i)
        paints.removeAt(i)
        return !pathBounds.isEmpty()
    }

    private fun updateValues(x: Float, y: Float, pressure: Float) {
        xCoord = x
        yCoord = y
        prevPressure = pressure

        xList.add(x)
        yList.add(y)
        pressureList.add(pressure)
    }

    private fun initStroke(x: Float, y: Float, pressure: Float, color: Int) {
        updateValues(x, y, pressure)
        paintColor = color
    }

    fun serializeData(): SerializedStroke {
        return SerializedStroke(
                xList,
                yList,
                pressureList,
                paintColor
        )
    }
}
