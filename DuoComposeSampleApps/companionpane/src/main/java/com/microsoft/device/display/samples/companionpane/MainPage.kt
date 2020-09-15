/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane

import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.ConstraintSet
import androidx.compose.foundation.layout.Dimension
import androidx.compose.foundation.layout.atMost
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.microsoft.device.display.samples.companionpane.model.DataProvider
import com.microsoft.device.display.samples.companionpane.viewmodel.AppStateViewModel

private lateinit var appStateViewModel: AppStateViewModel

@Composable
fun SetupUI(viewModel: AppStateViewModel) {
    appStateViewModel = viewModel
    val models = DataProvider.slides
    val isScreenSpannedLiveData = appStateViewModel.getIsScreenSpannedLiveData()
    val isScreenSpanned = isScreenSpannedLiveData.observeAsState(initial = false).value

    if (isScreenSpanned) {
//        ShowDetailWithList(models)
    } else {
        ImagePanel()
    }
}

@Composable
fun ImagePanel() {
    Image(asset = vectorResource(R.drawable.ic_photo_blue_24dp),
          modifier = Modifier.fillMaxWidth(),
          contentScale = ContentScale.FillWidth)
    SliderControl()
//        ControlPanel()
}

@Composable
fun ControlPanel() {
    ConstraintLayout(ConstraintSet {
        val slider1 = createRefFor("slider1")
        val slider2 = createRefFor("slider2")
        val button1 = createRefFor("button1")

        constrain(slider1) {
            start.linkTo(parent.start, margin = 10.dp)
            end.linkTo(slider2.start, margin = 10.dp)
        }
        constrain(slider2) {
            end.linkTo(parent.end, margin = 10.dp)
        }

        val barrier = createBottomBarrier(slider1, slider2)
        constrain(button1) {
            top.linkTo(barrier, margin = 10.dp)
            centerHorizontallyTo(parent)
            width = Dimension.preferredWrapContent.atMost(40.dp)
        }
    }) {
//        SliderControl(Modifier.layoutId("slider1"))
//        SliderControl(Modifier.layoutId("slider2"))

        Text("Text2", Modifier.layoutId("text2"))
//        Text("This is a very long text", Modifier.layoutId("text3"))
    }
}

@Composable fun SliderControl() {
    var sliderPosition by remember { mutableStateOf(0f) }
    Text(text = sliderPosition.toString())
    Slider(
        value = sliderPosition,
        onValueChange = { sliderPosition = it },
        valueRange = 0f..100f,
        thumbColor = MaterialTheme.colors.secondary,
        activeTrackColor = MaterialTheme.colors.secondary,
        modifier = Modifier.width(300.dp))
}
