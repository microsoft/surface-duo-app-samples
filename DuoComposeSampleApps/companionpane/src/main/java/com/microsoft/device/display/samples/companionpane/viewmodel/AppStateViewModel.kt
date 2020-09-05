/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppStateViewModel : ViewModel() {

    private val noteSelectionLiveData = MutableLiveData<Int>() // observe the image selection change
    private val isScreenSpannedLiveData = MutableLiveData<Boolean>() // observe the screen spanning mode

    fun getNoteSelectionLiveData(): LiveData<Int> {
        return this.noteSelectionLiveData
    }

    fun setNoteSelectionLiveData(selectedNote: Int) {
        noteSelectionLiveData.value = selectedNote
    }

    fun getIsScreenSpannedLiveData(): LiveData<Boolean> {
        return this.isScreenSpannedLiveData
    }

    fun setIsScreenSpannedLiveData(isScreenSpanned: Boolean) {
        isScreenSpannedLiveData.value = isScreenSpanned
    }
}