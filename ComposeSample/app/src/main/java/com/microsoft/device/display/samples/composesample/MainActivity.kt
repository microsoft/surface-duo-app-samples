/*
 *
 *  * Copyright (c) Microsoft Corporation. All rights reserved.
 *  * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.composesample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.window.WindowManager
import com.microsoft.device.display.samples.composesample.ui.DuoSDKSampleAppsTheme
import com.microsoft.device.display.samples.composesample.viewModels.AppStateViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var windowManager: WindowManager
    private lateinit var appStateViewModel: AppStateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        windowManager = WindowManager(this, null)
        appStateViewModel = ViewModelProvider(this).get(AppStateViewModel::class.java)

        super.onCreate(savedInstanceState)

        val isAppSpanned = isAppSpanned()
        appStateViewModel.setIsScreenSpannedLiveData(isAppSpanned)
        Log.i("ComposeSample", "onCreate, isAppSpanned: $isAppSpanned")

        setContent {
            DuoSDKSampleAppsTheme {
                Home(appStateViewModel)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val isAppSpanned = isAppSpanned()
        Log.i("ComposeSample", "onAttachedToWindow, isAppSpanned: $isAppSpanned")
        appStateViewModel.setIsScreenSpannedLiveData(isAppSpanned)
    }

    private fun isAppSpanned(): Boolean {
        try {
            val windowLayoutInfo = windowManager.windowLayoutInfo // only working after attached to window
            if (windowLayoutInfo.displayFeatures.size > 0) {
                return true
            }
        } catch (ex: Exception) {
            return false
        }
        return false
    }
}
