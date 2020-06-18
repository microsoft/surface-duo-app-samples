package com.microsoft.device.display.samples.sourceeditor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScrollViewModel : ViewModel() {
    private var _scrollPercentage = MutableLiveData<ScrollState>()

    fun setScroll(scrollKey: String, scrollVal: Int) {
        _scrollPercentage.setValue(ScrollState(scrollKey, scrollVal));
    }

    fun getScroll(): LiveData<ScrollState> {
        return _scrollPercentage
    }
}