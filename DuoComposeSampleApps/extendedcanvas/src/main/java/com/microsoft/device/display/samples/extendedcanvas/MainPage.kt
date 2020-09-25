package com.microsoft.device.display.samples.extendedcanvas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import com.microsoft.device.display.samples.extendedcanvas.viewmodel.AppStateViewModel

@Composable
fun MainPage(viewModel: AppStateViewModel) {
    val isScreenPortraitLiveData = viewModel.getIsScreenPortraitLiveData()
    val isScreenPortrait = isScreenPortraitLiveData.observeAsState(initial = true).value
    ScaleImage(isScreenPortrait)
}

@Composable
fun ScaleImage(isPortrait: Boolean) {
    if (isPortrait) {
        Image(asset = imageResource(R.drawable.new_york),
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.FillHeight,
              alignment = Alignment.Center)
    } else {
        Image(asset = imageResource(R.drawable.new_york),
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.FillWidth,
              alignment = Alignment.Center)
    }
}

