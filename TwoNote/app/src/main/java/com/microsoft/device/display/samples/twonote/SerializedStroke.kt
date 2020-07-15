package com.microsoft.device.display.samples.twonote

import java.io.Serializable

data class SerializedStroke (
        val xList: MutableList<Float> = mutableListOf(),
        val yList: MutableList<Float> = mutableListOf(),
        val pressureList: MutableList<Float> = mutableListOf(),
        val paintColor: Int = 0
) : Serializable