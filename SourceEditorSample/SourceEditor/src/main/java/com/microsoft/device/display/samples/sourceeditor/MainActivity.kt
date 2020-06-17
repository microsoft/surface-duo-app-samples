/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.sourceeditor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity


//class MainActivity : AppCompatActivity(), OnPageChangeListener {
class MainActivity : AppCompatActivity() {

    private lateinit var single: View
    private lateinit var dual: View

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        single = layoutInflater.inflate(R.layout.activity_main, null)
        setContentView(single)

        val rootView = window.decorView.rootView

        // Listen to Insets changes, this will fire when the soft keyboard pops (also other insets
        rootView.setOnApplyWindowInsetsListener { v, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val stableInsets: Insets
                stableInsets = insets.stableInsets
                val systemWindowInsets = insets.systemWindowInsets
                Log.d("VMDMAIN", "inset detected: " + stableInsets.toString())
            }
            insets
        }

        //final View activityRootView = findViewById(R.id.activityRoot);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener {
            @Override
            fun onGlobalLayout() {
                val heightDiff = rootView. getRootView ().getHeight() - rootView.getHeight();
                if (heightDiff > dpToPx(this, 200f)) { // if more than 200 dp, it's probably a keyboard...
                    Log.d("VMDMAIN", "resize detected: ")// ... do something here
                }
            }
        }
    }


    fun dpToPx(context: Context, valueInDp: Float): Float {
        val metrics: DisplayMetrics = context.getResources().getDisplayMetrics()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
    }
}