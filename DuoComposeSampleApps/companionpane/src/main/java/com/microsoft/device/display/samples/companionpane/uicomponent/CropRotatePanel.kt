/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane.uicomponent

import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microsoft.device.display.samples.companionpane.ui.Gray

@Composable
fun CropRotateSpannedLandscapePanel(modifier: Modifier) {
    Column(modifier = modifier,
           verticalArrangement = Arrangement.spacedBy(5.dp)) {
        CropRotatePanel(Modifier.fillMaxWidth())
        ImageButton("Reset", R.drawable.ic_menu_revert, Modifier.fillMaxWidth())
        FilterTopPanel()
    }
}

@Composable
fun CropRotateSpannedPortraitPanel(modifier: Modifier) {
    Column(modifier = modifier,
           verticalArrangement = Arrangement.spacedBy(5.dp)) {
        CropRotatePanel(Modifier.fillMaxWidth())
        ImageButton("Reset", R.drawable.ic_menu_revert, Modifier.fillMaxWidth())
    }
}

@Composable
fun CropRotatePanel(modifier: Modifier) {
    Column(modifier = modifier,
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(text = "Crop & Rotate",
             textAlign = TextAlign.Center,
             color = Color.White,
             fontSize = 12.sp)
        Divider(color = Color.White, thickness = 1.dp)
        Spacer(Modifier.preferredWidth(5.dp))
        SliderControl()
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            ImageButton("Rotate", R.drawable.ic_menu_rotate, Modifier.weight(1f))
            ImageButton("Flip", com.microsoft.device.display.samples.companionpane.R.drawable.ic_flip, Modifier.weight(1f))
        }
        Button(onClick = {},
               backgroundColor = Gray,
               modifier = Modifier.fillMaxWidth().height(40.dp)) {
            Image(asset = imageResource(id = com.microsoft.device.display.samples.companionpane.R.drawable.ic_aspect_ratio),
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.size(20.dp, 20.dp))
            Spacer(Modifier.preferredWidth(8.dp))
            Text("Aspect ratio: Custom",
                 color = Color.White,
                 fontSize = 12.sp)
            Spacer(Modifier.preferredWidth(8.dp))
            Image(asset = imageResource(id = com.microsoft.device.display.samples.companionpane.R.drawable.arrow_down),
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.size(16.dp, 16.dp)
            )
        }
    }
}