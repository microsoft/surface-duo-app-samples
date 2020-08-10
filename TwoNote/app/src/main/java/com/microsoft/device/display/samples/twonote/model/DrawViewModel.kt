/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.model

import androidx.lifecycle.ViewModel
import com.microsoft.device.display.samples.twonote.structures.Stroke

class DrawViewModel : ViewModel() {
    private var strokeList = listOf<Stroke>() // to retain the drawing when spanning/unspanning

    fun getStrokeList(): List<Stroke> {
        return strokeList
    }

    fun setStrokeList(s: List<Stroke>) {
        strokeList = s
    }
}
