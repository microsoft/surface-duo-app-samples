/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microsoft.device.display.samples.companionpane.model.DataProvider
import com.microsoft.device.display.samples.companionpane.model.Slide
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
        SlidesPager(models = models)
    }
}

@Composable
fun SlidesPager(models: List<Slide>) {
    LazyRowFor(items = models
    ) { item ->
        SlidePage(model = item)
    }
}

@Composable
fun SlidePage(model: Slide) {
    Card(modifier = Modifier.width(530.dp).fillMaxHeight().padding(start = 16.dp, top = 32.dp, bottom = 32.dp, end = 16.dp),
         elevation = 8.dp){
        Text(text = model.note, 
             modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
    }
}

@Composable
fun NoteList(models: List<Slide>) {
    LazyColumnForIndexed(
        items = models,
        modifier = Modifier.fillMaxHeight().fillMaxWidth()
    ) { index, item ->
        Text(item.note, modifier = Modifier.fillMaxHeight().wrapContentSize(Alignment.Center), fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}