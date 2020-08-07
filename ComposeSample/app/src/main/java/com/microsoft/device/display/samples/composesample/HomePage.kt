/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.composesample

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.microsoft.device.display.samples.composesample.models.DataProvider
import com.microsoft.device.display.samples.composesample.models.ImageModel
import com.microsoft.device.display.samples.composesample.viewModels.AppStateViewModel

private lateinit var appStateViewModel: AppStateViewModel
private const val SELECT_IMAGE = 1000

@Preview
@Composable
fun HomePreview() {
    val models = DataProvider.imageModels
    ShowList(models = models)
}

@Composable
fun Home(viewModel: AppStateViewModel) {
    appStateViewModel = viewModel
    SetupUI()
}

@Composable
fun SetupUI() {
    val models = DataProvider.imageModels
    val isScreenSpannedLiveData = appStateViewModel.getIsScreenSpannedLiveData()
    val isScreenSpanned = isScreenSpannedLiveData.observeAsState(initial = false).value

    Log.i("ComposeSample", "SetupUI isScreenSpanned: $isScreenSpanned")

    if (isScreenSpanned) {
        ShowDetailWithList(models)
    } else {
        ShowList(models)
    }
}

@Composable
private fun ShowList(models: List<ImageModel>) {
    Log.i("ComposeSample", "show list")

    ShowListColumn(models, Modifier.fillMaxHeight() then Modifier.fillMaxWidth())
}

@Composable
private fun ShowListColumn(models: List<ImageModel>, modifier: Modifier) {
    ScrollableColumn(modifier) {
        models.forEachIndexed { index, model ->
            Row(
                modifier = Modifier.clickable(
                    onClick = {
                        appStateViewModel.setImageSelectionLiveData(index)
                    }
                ) then Modifier.fillMaxWidth(),
                verticalGravity = Alignment.CenterVertically
            ) {
                Image(asset = imageResource(model.image), modifier = Modifier.preferredHeight(100.dp).preferredWidth(150.dp))
                Spacer(Modifier.preferredWidth(16.dp))
                Text(model.id, modifier = Modifier.fillMaxHeight().wrapContentSize(Alignment.Center))
                Spacer(Modifier.preferredWidth(16.dp))
                Text(model.title, modifier = Modifier.fillMaxHeight().wrapContentSize(Alignment.Center))
            }
            Divider(color = Color.LightGray)
        }
    }
}

@Composable
fun ShowDetailWithList(models: List<ImageModel>) {
    Log.i("ComposeSample", "show detail")

    val imageSelectionLiveData = appStateViewModel.getImageSelectionLiveData()
    val selectedIndex = imageSelectionLiveData.observeAsState(initial = 0).value
    val selectedImageModel = models[selectedIndex]

    Row(modifier = Modifier.fillMaxHeight().wrapContentSize(Alignment.Center) then Modifier.fillMaxWidth().wrapContentSize(Alignment.Center)) {
        ShowListColumn(models, Modifier.fillMaxHeight().wrapContentSize(Alignment.Center).weight(1f))
        Column(modifier = Modifier.fillMaxHeight().wrapContentSize(Alignment.Center).weight(1f), horizontalGravity = Alignment.CenterHorizontally) {
            Text(text = selectedImageModel.id, fontSize = 60.sp)
            Image(asset = imageResource(selectedImageModel.image))
        }
    }
}
