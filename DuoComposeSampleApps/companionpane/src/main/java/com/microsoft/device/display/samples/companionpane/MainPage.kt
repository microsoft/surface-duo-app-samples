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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Divider
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microsoft.device.display.samples.companionpane.model.DataProvider
import com.microsoft.device.display.samples.companionpane.ui.Gray
import com.microsoft.device.display.samples.companionpane.ui.blueTint
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
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            Spacer(Modifier.preferredWidth(5.dp))
            CropRotatePanel(Modifier.weight(1f))
            AdjustmentsPanel(Modifier.weight(1f))
            Spacer(Modifier.preferredWidth(5.dp))
        }
    }
}

@Composable
fun ImagePanel(modifier: Modifier) {
    Image(asset = vectorResource(R.drawable.ic_photo_blue_24dp),
          modifier = Modifier.fillMaxWidth().then(modifier),
          contentScale = ContentScale.Fit,
          colorFilter = ColorFilter.tint(blueTint))
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
        Divider(color = Color.White, thickness = 1.dp)
        Spacer(Modifier.preferredWidth(5.dp))
        SliderControl("Straightening")
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            Button(modifier = Modifier.fillMaxWidth().weight(1f).height(40.dp),
                   onClick = { print("test") },
                   backgroundColor = Gray) {
                Image(asset = imageResource(id = android.R.drawable.ic_menu_rotate), contentScale = ContentScale.Fit)
                Spacer(Modifier.preferredWidth(8.dp))
                Text("Rotate", color = Color.White, fontSize = 12.sp)
            }
            Button(modifier = Modifier.fillMaxWidth().weight(1f).height(40.dp),
                   onClick = { print("test") },
                   backgroundColor = Gray) {
                Image(asset = imageResource(id = R.drawable.ic_flip),
                      contentScale = ContentScale.Fit,
                      modifier = Modifier.size(20.dp, 20.dp))
                Spacer(Modifier.preferredWidth(8.dp))
                Text("Flip", color = Color.White, fontSize = 12.sp)
            }
        }
        Button(onClick = { print("test") },
               backgroundColor = Gray,
               modifier = Modifier.fillMaxWidth().weight(1f).height(40.dp)) {
            Image(asset = imageResource(id = R.drawable.ic_aspect_ratio),
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.size(20.dp, 20.dp))
            Spacer(Modifier.preferredWidth(8.dp))
            Text("Aspect ratio: Custom", color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.preferredWidth(8.dp))
            Image(asset = imageResource(id = R.drawable.arrow_down),
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.size(16.dp, 16.dp)
            )
        }
    }
}

@Composable fun SliderControl(title: String) {
    var sliderPosition by remember { mutableStateOf(0f) }
    Column() {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title,
                 textAlign = TextAlign.Left,
                 color = Color.White,
                 fontSize = 12.sp)
            Text(text = sliderPosition.toString(),
                 textAlign = TextAlign.Right,
                 color = Color.White,
                 fontSize = 12.sp)
        }
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 0f..100f,
            thumbColor = Gray,
            activeTrackColor = Gray,
            inactiveTrackColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AdjustmentsPanel(modifier: Modifier) {
    Column(modifier = modifier,
           horizontalGravity = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.spacedBy(space = 5.dp)) {
        Text(text = "Adjustments", textAlign = TextAlign.Center, color = Color.White, fontSize = 12.sp)
        Divider(color = Color.White, thickness = 1.dp)
        Spacer(Modifier.preferredWidth(5.dp))
        AdjustmentControl("Light")
        ImagePanel(Modifier.height(40.dp))
        AdjustmentControl("Color")
        ImagePanel(Modifier.height(40.dp))
    }
}

@Composable fun AdjustmentControl(title: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start) {
        Image(asset = imageResource(id = R.drawable.arrow_right),
              contentScale = ContentScale.Fit,
              modifier = Modifier.size(16.dp, 16.dp))
        Spacer(Modifier.preferredWidth(5.dp))
        Text(text = title, textAlign = TextAlign.Left, color = Color.White, fontSize = 12.sp)
    }
}