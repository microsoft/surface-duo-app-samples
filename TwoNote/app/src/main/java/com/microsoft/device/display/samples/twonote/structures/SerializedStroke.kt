package com.microsoft.device.display.samples.twonote.structures

import java.io.Serializable

data class SerializedStroke(
    val xList: List<MutableList<Float>> = listOf(),
    val yList: List<MutableList<Float>> = listOf(),
    val pressureList: List<MutableList<Float>> = listOf(),
    val paintColor: Int = 0,
    val thicknessMultiplier: Int = 25,
    val rotated: Boolean = false,
    val highlightStroke: Boolean = false
) : Serializable