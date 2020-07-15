package com.microsoft.device.display.samples.twonote

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import java.io.Serializable

class Stroke (x: Float, y: Float, pressure: Float, color: Int) : Serializable {

    companion object {
        private var pathList: MutableList<Path> = mutableListOf()
        private var paints: MutableList<Paint> = mutableListOf()
        private var pathBounds: MutableList<RectF> = mutableListOf()
        private var paintColor: Int = 0
    }

    private var xCoord: Float = 0f
    private var yCoord: Float = 0f
    private var prevPressure: Float = 0f

    init {
        val path = Path()
        path.moveTo(x, y)
        updateValues(x, y, pressure)
        paintColor = color
    }

    fun continueDrawing(x: Float, y: Float, pressure: Float) {
        if (pressure == prevPressure && !pathList.isEmpty())
            continueJoint(x, y)
        else
            addJoint(x, y, pressure)
    }

    fun finishStroke(x: Float, y: Float) {
        if (!pathList.isEmpty()) {
            val bounds = RectF()
            pathList[pathList.lastIndex].computeBounds(bounds, true)
            pathBounds.removeAt(pathBounds.lastIndex)
            pathBounds.add(bounds)
        }
    }

    private fun addJoint(x: Float, y: Float, pressure: Float) {
        if (!pathList.isEmpty()) {
            val bounds = RectF()
            pathList[pathList.lastIndex].computeBounds(bounds, true)
            pathBounds.removeAt(pathBounds.lastIndex)
            pathBounds.add(bounds)
        }

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
    }
}
