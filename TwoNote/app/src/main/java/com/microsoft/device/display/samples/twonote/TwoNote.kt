package com.microsoft.device.display.samples.twonote

import android.app.Application
import com.microsoft.device.dualscreen.fragmentshandler.FragmentManagerStateHandler
import com.microsoft.device.dualscreen.core.manager.SurfaceDuoScreenManager

class TwoNote: Application() {
    lateinit var surfaceDuoScreenManager: SurfaceDuoScreenManager

    override fun onCreate() {
        super.onCreate()
        surfaceDuoScreenManager = SurfaceDuoScreenManager.getInstance(this)
        FragmentManagerStateHandler.initialize(this, surfaceDuoScreenManager)
    }
}