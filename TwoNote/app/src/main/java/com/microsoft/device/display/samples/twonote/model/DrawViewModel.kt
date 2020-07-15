/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.model

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.microsoft.device.display.samples.twonote.Stroke

class DrawViewModel : ViewModel() {
    private val imageLiveData = MutableLiveData<Bitmap?>() // to copy between two screens
    private var strokeList = listOf<Stroke>() // to retain the drawing when spanning/unspanning
    private var penRadius: Int = 0 // to retain the pen value when spanning/unspanning

    fun getImageLiveData(): LiveData<Bitmap?> {
        return this.imageLiveData
    }

    fun setImageLiveData(bitmap: Bitmap?) {
        imageLiveData.value = bitmap
    }

    fun getStrokeList(): List<Stroke> {
        return strokeList
    }

    fun setStrokeList(s: List<Stroke>) {
        strokeList = s
    }

    fun getPenRadius(): Int {
        return penRadius
    }

    fun setPenRadius(radius: Int) {
        penRadius = radius
    }
}