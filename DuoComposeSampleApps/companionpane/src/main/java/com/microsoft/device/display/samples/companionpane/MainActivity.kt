/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *  *
 *
 */

package com.microsoft.device.display.samples.companionpane

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.core.util.Consumer
import androidx.lifecycle.ViewModelProvider
import androidx.window.WindowLayoutInfo
import androidx.window.WindowManager
import com.microsoft.device.display.samples.companionpane.ui.CompanionPaneAppsTheme
import com.microsoft.device.display.samples.companionpane.viewmodel.AppStateViewModel
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var windowManager: WindowManager
    private lateinit var appStateViewModel: AppStateViewModel

    private val handler = Handler(Looper.getMainLooper())
    private val mainThreadExecutor = Executor { r: Runnable -> handler.post(r) }
    private val layoutStateChangeCallback = LayoutStateChangeCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        windowManager = WindowManager(this, null)
        appStateViewModel = ViewModelProvider(this).get(AppStateViewModel::class.java)
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        appStateViewModel.setIsScreenPortraitLiveData(isPortrait)

        super.onCreate(savedInstanceState)
        setContent {
            CompanionPaneAppsTheme {
                Scaffold (
                    topBar = { TopAppBar(
                        title = { Text(stringResource(R.string.app_name)) }
                        )
                    },
                    bodyContent = { SetupUI(viewModel = appStateViewModel)}
                )
            }
        }
    }

    // Checks the orientation of the screen
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            appStateViewModel.setIsScreenPortraitLiveData(false)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            appStateViewModel.setIsScreenPortraitLiveData(true)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        windowManager.registerLayoutChangeCallback(mainThreadExecutor, layoutStateChangeCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        windowManager.unregisterLayoutChangeCallback(layoutStateChangeCallback)
    }

    inner class LayoutStateChangeCallback : Consumer<WindowLayoutInfo> {
        override fun accept(newLayoutInfo: WindowLayoutInfo) {
            val isScreenSpanned = newLayoutInfo.displayFeatures.size > 0
            appStateViewModel.setIsScreenSpannedLiveData(isScreenSpanned)
        }
    }
}