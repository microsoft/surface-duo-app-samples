package com.microsoft.device.display.samples.twonote.structures

import java.io.Serializable

data class SerializedImage(
    val name: String,
    val image: String,
    val coords: List<Float> = listOf(),
    val dimens: List<Int> = listOf()
) : Serializable