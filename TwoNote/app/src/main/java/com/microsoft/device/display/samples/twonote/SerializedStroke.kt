package com.microsoft.device.display.samples.twonote

import java.io.Serializable

data class SerializedStroke(
        val xList: List<MutableList<Float>> = listOf(),
        val yList: List<MutableList<Float>> = listOf(),
        val pressureList: List<MutableList<Float>> = listOf(),
        val paintColor: Int = 0
) : Serializable
