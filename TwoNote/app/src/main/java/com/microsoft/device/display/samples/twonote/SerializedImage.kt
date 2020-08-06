package com.microsoft.device.display.samples.twonote

import java.io.Serializable

data class SerializedImage(
    val authority: String,
    val path: String,
    val coords: List<Float> = listOf(),
    val dimens: List<Int> = listOf()
) : Serializable