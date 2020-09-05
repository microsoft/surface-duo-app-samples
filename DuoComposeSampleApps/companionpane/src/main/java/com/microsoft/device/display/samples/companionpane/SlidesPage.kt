/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane

import android.util.Log
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.microsoft.device.display.samples.companionpane.model.DataProvider
import com.microsoft.device.display.samples.companionpane.model.Slide
import com.microsoft.device.display.samples.companionpane.ui.DuoComposeSampleAppsTheme
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
        NoteList(models = models)
    }
}

@Composable
fun NoteList(models: List<Slide>) {
    LazyColumnForIndexed(
        items = models,
        modifier = Modifier.fillMaxHeight() then Modifier.fillMaxWidth()
    ) { index, item ->
        Text(item.note, modifier = Modifier.fillMaxHeight().wrapContentSize(Alignment.Center), fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

