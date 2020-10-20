/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane.uicomponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.microsoft.device.display.samples.companionpane.R

@Composable
fun MagicDefinitionPanel() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        MagicWandPanel()
        DefinitionPanel()
    }
}

val iconWidth = 25.dp
val controlWidth = 70.dp

@Composable
fun MagicWandPanel() {
    Row() {
        ImageWithText(R.drawable.filter_icon, "Magic Wand", iconWidth, controlWidth)
        SliderControl()
    }
}

@Composable
fun DefinitionPanel() {
    Row() {
        ImageWithText(R.drawable.hdr_icon, "Definition", iconWidth, controlWidth)
        SliderControl()
    }
}

@Composable
fun VignetteBrightnessPanel() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        VignettePanel()
        BrightnessPanel()
    }
}

@Composable
fun VignettePanel() {
    Row() {
        ImageWithText(R.drawable.zoom_icon, "Vignette", iconWidth, controlWidth)
        SliderControl()
    }
}

@Composable
fun BrightnessPanel() {
    Row() {
        ImageWithText(R.drawable.brightness_icon, "Brightness", iconWidth, controlWidth)
        SliderControl()
    }
}