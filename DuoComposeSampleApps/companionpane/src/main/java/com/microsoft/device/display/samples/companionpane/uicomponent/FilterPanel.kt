/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane.uicomponent

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope.weight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microsoft.device.display.samples.companionpane.ImagePanel

@Composable
fun FilterPanel(modifier: Modifier) {
    Column(modifier = modifier,
        horizontalGravity = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        FilterTopPanel()
        FilterBottomPanel(modifier = Modifier.fillMaxWidth(),
                          imageWidth = 20.dp,
                          imageHeight = 120.dp)
    }
}

@Composable
fun FilterTopPanel() {
    Column(horizontalGravity = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        Text(
            text = "Filter",
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 12.sp
        )
        Divider(color = Color.White, thickness = 1.dp)
        Spacer(Modifier.preferredWidth(5.dp))
        LeftAlignText("Straightening")
        ImagePanel(Modifier.height(40.dp))
        Spacer(Modifier.preferredWidth(5.dp))
        SliderControl(title = "Filter intensity")
    }
}

@Composable
fun FilterBottomPanel(modifier: Modifier, imageWidth: androidx.compose.ui.unit.Dp, imageHeight: androidx.compose.ui.unit.Dp) {
    Column(modifier = modifier) {
        LeftAlignText("Choose a filter")
        ImageRow(imageWidth, imageHeight)
        ImageRow(imageWidth, imageHeight)
    }
}

@Composable
fun LeftAlignText(title: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start) {
        Text(text = title, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun ImageRow(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier.height(height).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalGravity = Alignment.CenterVertically
    ) {
        SmallEvenImage(1f, width)
        SmallEvenImage(1f, width)
        SmallEvenImage(1f, width)
        SmallEvenImage(1f, width)
        SmallEvenImage(1f, width)
        SmallEvenImage(1f, width)
    }
}

@Composable
fun SmallEvenImage(weightPercent: Float, width: androidx.compose.ui.unit.Dp) {
    ImagePanel(Modifier.width(width).fillMaxHeight().weight(weightPercent))
}