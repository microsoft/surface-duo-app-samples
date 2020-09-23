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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.layout.size
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
import com.microsoft.device.display.samples.companionpane.ImagePanel
import com.microsoft.device.display.samples.companionpane.R

@Composable
fun AdjustmentsSpannedExtendPanel(modifier: Modifier) {
    Column(modifier = Modifier.fillMaxHeight().then(modifier),
           verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Spacer(Modifier.preferredWidth(10.dp))
        AdjustmentsExtendPanel(Modifier.fillMaxWidth())
        ImageButton("Red eyd", R.drawable.ic_eye, Modifier.fillMaxWidth())
        ImageButton("Spot fix", R.drawable.ic_tap, Modifier.fillMaxWidth())
    }
}

@Composable
fun AdjustmentsExtendPanel(modifier: Modifier) {
    Column(modifier = modifier,
           verticalArrangement = Arrangement.spacedBy(5.dp)) {
        AdjustmentsPanel(Modifier.fillMaxWidth())
        SliderControl("Clarity")
        SliderControl("Saturation")
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

@Composable
fun AdjustmentControl(title: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start) {
        Image(asset = imageResource(id = R.drawable.arrow_right),
              contentScale = ContentScale.Fit,
              modifier = Modifier.size(16.dp, 16.dp))
        Spacer(Modifier.preferredWidth(5.dp))
        Text(text = title, textAlign = TextAlign.Left, color = Color.White, fontSize = 12.sp)
    }
}