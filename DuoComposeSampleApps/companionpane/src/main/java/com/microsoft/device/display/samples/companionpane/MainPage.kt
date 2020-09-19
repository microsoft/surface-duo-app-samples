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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microsoft.device.display.samples.companionpane.model.DataProvider
import com.microsoft.device.display.samples.companionpane.ui.Gray
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
        PortraitLayout()
    }
}

@Composable
fun PortraitLayout() {
    Column(modifier = Modifier.fillMaxSize()) {
        ImagePanel(Modifier.height(400.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            CropRotatePanel(Modifier.weight(1f))
            AdjustmentsPanel(Modifier.weight(1f))
        }
    }
}

@Composable
fun ImagePanel(modifier: Modifier) {
    Image(asset = vectorResource(R.drawable.ic_photo_blue_24dp),
          modifier = Modifier.fillMaxWidth().then(modifier),
          contentScale = ContentScale.Fit)
}

@Composable
fun CropRotatePanel(modifier: Modifier) {
    Column(modifier = modifier,
           horizontalGravity = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.spacedBy(space = 5.dp)) {
        Text(text = "Crop & Rotate",
             textAlign = TextAlign.Center,
             color = Color.White,
             fontSize = 12.sp)
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            drawLine(
//                Color.Black, Offset(20f, 0f),
//                Offset(350f, 0f), strokeWidth = 5f
//            )
//        }
        Text(text = "Straightening", textAlign = TextAlign.Center, color = Color.White, fontSize = 12.sp)
        SliderControl()
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            Button(modifier = Modifier.fillMaxWidth().weight(1f),
                   onClick = { print("test") },
                   backgroundColor = Gray) {
                Text("Rotate", color = Color.White, fontSize = 12.sp)
            }
            Button(modifier = Modifier.fillMaxWidth().weight(1f),
                   onClick = { print("test") },
                   backgroundColor = Gray) {
                Text("Flip", color = Color.White, fontSize = 12.sp)
            }
        }
        Button(onClick = { print("test") },
               backgroundColor = Gray) {
            Text("Aspect ratio: Custom", color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
fun AdjustmentsPanel(modifier: Modifier) {
    Column(modifier = modifier,
           horizontalGravity = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.spacedBy(space = 5.dp)) {
        Text(text = "Adjustments", textAlign = TextAlign.Center, color = Color.White, fontSize = 12.sp)
        Text(text = "Light", textAlign = TextAlign.Left, color = Color.White, fontSize = 12.sp)
        ImagePanel(Modifier.height(40.dp))
        Text(text = "Color", textAlign = TextAlign.Left, color = Color.White, fontSize = 12.sp)
        ImagePanel(Modifier.height(40.dp))
    }
}

@Composable fun SliderControl() {
    var sliderPosition by remember { mutableStateOf(0f) }
    Column() {
        Text(text = sliderPosition.toString(),
             textAlign = TextAlign.Right,
             color = Color.White,
             fontSize = 12.sp)
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 0f..100f,
            thumbColor = Gray,
            activeTrackColor = Gray,
            inactiveTrackColor = Color.White,
            modifier = Modifier.width(250.dp)
        )
    }
}
