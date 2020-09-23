/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope.weight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.microsoft.device.display.samples.companionpane.ui.blueTint
import com.microsoft.device.display.samples.companionpane.uicomponent.AdjustmentsExtendPanel
import com.microsoft.device.display.samples.companionpane.uicomponent.AdjustmentsPanel
import com.microsoft.device.display.samples.companionpane.uicomponent.AdjustmentsSpannedExtendPanel
import com.microsoft.device.display.samples.companionpane.uicomponent.CropRotateExtendPanel
import com.microsoft.device.display.samples.companionpane.uicomponent.CropRotatePanel
import com.microsoft.device.display.samples.companionpane.uicomponent.FilterPanel
import com.microsoft.device.display.samples.companionpane.viewmodel.AppStateViewModel

private lateinit var appStateViewModel: AppStateViewModel

@Composable
fun SetupUI(viewModel: AppStateViewModel) {
    appStateViewModel = viewModel
    val isScreenSpannedLiveData = appStateViewModel.getIsScreenSpannedLiveData()
    val isScreenSpanned = isScreenSpannedLiveData.observeAsState(initial = false).value

    val isScreenPortraitLiveData = appStateViewModel.getIsScreenPortraitLiveData()
    val isScreenPortrait = isScreenPortraitLiveData.observeAsState(initial = true).value

    if (isScreenSpanned) {
        if (isScreenPortrait) {
            PortraitSpannedLayout()
        } else {
            LandscapeSpannedLayout()
        }
    } else {
        if (isScreenPortrait) {
            PortraitLayout()
        } else {
            LandscapeLayout()
        }
    }
}

@Composable
fun LandscapeSpannedLayout() {
    Row(Modifier.fillMaxSize()) {
        ImagePanel(modifier = Modifier.fillMaxSize().weight(1f))
        Row(modifier = Modifier.fillMaxSize().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalGravity = Alignment.Top) {
            Spacer(Modifier.preferredWidth(10.dp))
            CropRotateExtendPanel(modifier = Modifier.fillMaxWidth().weight(1/3f))
            AdjustmentsSpannedExtendPanel(modifier = Modifier.fillMaxWidth().weight(1/3f))
            FilterPanel(modifier = Modifier.fillMaxWidth().weight(1/3f))
            Spacer(Modifier.preferredWidth(5.dp))
        }
    }
}

@Composable
fun PortraitSpannedLayout() {
    Column(Modifier.fillMaxSize()) {
        ImagePanel(modifier = Modifier.fillMaxSize().weight(1f))
        CropRotatePanel(modifier = Modifier.fillMaxSize().weight(1f))
    }
}

@Composable
fun PortraitLayout() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
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
fun LandscapeLayout() {
    Row() {
        ImagePanel(Modifier.height(400.dp).weight(0.6f))
        AdjustmentsExtendPanel(Modifier.weight(0.4f))
        Spacer(Modifier.preferredWidth(8.dp))
    }
}

@Composable
fun ImagePanel(modifier: Modifier) {
    Image(asset = vectorResource(R.drawable.ic_photo_blue_24dp),
          modifier = Modifier.fillMaxWidth().then(modifier),
          contentScale = ContentScale.Fit,
          colorFilter = ColorFilter.tint(blueTint),
          alignment = Alignment.Center)
}

