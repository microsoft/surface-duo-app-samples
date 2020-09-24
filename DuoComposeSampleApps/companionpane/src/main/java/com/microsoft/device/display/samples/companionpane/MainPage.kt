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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.layout.width
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
import com.microsoft.device.display.samples.companionpane.uicomponent.CropRotatePanel
import com.microsoft.device.display.samples.companionpane.uicomponent.CropRotateSpannedLandscapePanel
import com.microsoft.device.display.samples.companionpane.uicomponent.CropRotateSpannedPortraitPanel
import com.microsoft.device.display.samples.companionpane.uicomponent.FilterBottomPanel
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
    Row(modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(50.dp)) {
        ImagePanel(modifier = Modifier.fillMaxHeight().weight(1f))
        Column(modifier = Modifier.fillMaxHeight().weight(1f)) {
            Spacer(Modifier.preferredHeight(8.dp))
            Row(modifier = Modifier.fillMaxWidth().weight(0.7f),
                horizontalArrangement= Arrangement.spacedBy(8.dp)) {
                CropRotateSpannedLandscapePanel(modifier = Modifier.fillMaxHeight().weight(1f))
                AdjustmentsSpannedExtendPanel(modifier = Modifier.fillMaxHeight().weight(1f))
                Spacer(Modifier.preferredWidth(5.dp))
            }
            FilterBottomPanel(modifier = Modifier.fillMaxWidth().weight(0.3f),
                              imageWidth = 70.dp,
                              imageHeight = 80.dp)
        }
    }
}

@Composable
fun PortraitSpannedLayout() {
    Column(Modifier.fillMaxSize(),
           verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ImagePanel(modifier = Modifier.fillMaxWidth().weight(1f))
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.preferredWidth(8.dp))
            CropRotateSpannedPortraitPanel(modifier = Modifier.fillMaxHeight().weight(1f))
            AdjustmentsSpannedExtendPanel(modifier = Modifier.fillMaxHeight().weight(1f))
            FilterPanel(modifier = Modifier.fillMaxHeight().weight(1f))
            Spacer(Modifier.preferredWidth(8.dp))
        }
    }
}

@Composable
fun PortraitLayout() {
    Column(modifier = Modifier.fillMaxSize(),
           verticalArrangement = Arrangement.Center) {
        ImagePanel(Modifier.height(420.dp).fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Spacer(Modifier.preferredWidth(5.dp))
            CropRotatePanel(Modifier.weight(1f))
            AdjustmentsPanel(Modifier.weight(1f))
            Spacer(Modifier.preferredWidth(5.dp))
        }
    }
}

@Composable
fun LandscapeLayout() {
    Row(modifier = Modifier.fillMaxSize()) {
        ImagePanel(Modifier.width(420.dp).fillMaxHeight().weight(0.65f))
        Column(Modifier.fillMaxHeight().weight(0.35f)) {
            Spacer(Modifier.preferredHeight(10.dp))
            AdjustmentsExtendPanel(Modifier.fillMaxSize())
        }
        Spacer(Modifier.preferredWidth(8.dp))
    }
}

@Composable
fun ImagePanel(modifier: Modifier) {
    Image(asset = vectorResource(R.drawable.ic_photo_blue_24dp),
          modifier = modifier,
          contentScale = ContentScale.Fit,
          colorFilter = ColorFilter.tint(blueTint),
          alignment = Alignment.Center)
}

