/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane.uicomponent

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import com.microsoft.device.display.samples.companionpane.R

@Composable
fun ImagePanel(modifier: Modifier) {
    Image(asset = imageResource(R.drawable.full_image),
          modifier = modifier,
          contentScale = ContentScale.Inside,
          alignment = Alignment.Center)
}