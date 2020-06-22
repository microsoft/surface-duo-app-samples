package com.microsoft.device.display.samples.photoeditor

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PhotoEditorVM : ViewModel() {
    private var image = MutableLiveData<Drawable>()

    fun updateImage(newImage: Drawable) {
        image.value = newImage
    }

    fun getImage(): MutableLiveData<Drawable> {
        return image
    }
}
