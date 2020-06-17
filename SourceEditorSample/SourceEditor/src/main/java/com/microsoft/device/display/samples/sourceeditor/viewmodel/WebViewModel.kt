package com.microsoft.device.display.samples.sourceeditor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WebViewModel : ViewModel() {
    private var _webFormState = MutableLiveData<String>()

    fun setText(htmlText: String) {
        _webFormState.setValue(htmlText);
        //Log.d("viewModelDSet", "" + _webFormState.value)
    }

    fun getText(): LiveData<String> {
        return _webFormState
    }
}

