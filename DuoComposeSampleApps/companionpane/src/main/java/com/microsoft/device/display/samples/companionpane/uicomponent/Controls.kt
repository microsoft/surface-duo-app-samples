/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane.uicomponent

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
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microsoft.device.display.samples.companionpane.ui.Gray

@Composable
fun SliderControl(title: String) {
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
fun ImageButton(title: String, imageId: Int, modifier: Modifier) {
    Button(modifier = Modifier.fillMaxWidth().height(40.dp).then(modifier),
           onClick = {},
           backgroundColor = Gray) {
        Image(asset = imageResource(id = imageId),
              contentScale = ContentScale.Fit,
              modifier = Modifier.size(20.dp, 20.dp))
        Spacer(Modifier.preferredWidth(8.dp))
        Text(title, color = Color.White, fontSize = 12.sp)
    }
}