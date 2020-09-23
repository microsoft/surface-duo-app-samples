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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.preferredWidth
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
    Column(
        modifier = modifier,
        horizontalGravity = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        Text(text = "Filter", textAlign = TextAlign.Center, color = Color.White, fontSize = 12.sp)
        Divider(color = Color.White, thickness = 1.dp)
        Spacer(Modifier.preferredWidth(5.dp))
        Text(text = "Straightening", textAlign = TextAlign.Left, color = Color.White, fontSize = 12.sp)
        ImagePanel(Modifier.height(40.dp))
        Spacer(Modifier.preferredWidth(5.dp))
        SliderControl(title = "Filter intensity")
        Text(text = "Choose a filter", textAlign = TextAlign.Left, color = Color.White, fontSize = 12.sp)
        Row(
            modifier = Modifier.height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
        }
        Row(
            modifier = Modifier.height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
            ImagePanel(Modifier.height(40.dp))
        }
    }
}