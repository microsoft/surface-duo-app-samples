/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.sourceeditor

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.ScrollView


import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.microsoft.device.display.samples.sourceeditor.viewmodel.ScrollViewModel
import com.microsoft.device.display.samples.sourceeditor.viewmodel.WebViewModel

import com.microsoft.device.dualscreen.layout.ScreenHelper

class PreviewFragment : Fragment() {
    private lateinit var webVM: WebViewModel
    private lateinit var scrollVM: ScrollViewModel
    private lateinit var scrollView: ScrollView

    private var scrollRange : Int = 1
    private var rangeFound : Boolean = false
    private var scrollingBuffer : Int = 2

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_preview, container, false)

        val webView: WebView = view.findViewById(R.id.webview_preview)
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()

        activity?.let { activity ->
            webVM = ViewModelProvider(requireActivity()).get(WebViewModel::class.java)
            scrollVM = ViewModelProvider(requireActivity()).get(ScrollViewModel::class.java)

            view.isFocusableInTouchMode = true

            val str : String? = (webVM.getText().value)
            webView.loadData(str, "text/html", "UTF-8")

            scrollView = view.findViewById(R.id.scrollview_preview)
            scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                Log.d("VMDPREVIEW", "Scrolling")
                handleScrolling(false, scrollY)
            }

            scrollVM.getScroll().observe(requireActivity(), Observer { state ->
                if(!state.scrollKey.equals("Preview")) {
                    Log.d("VMDPREVIEW", "data received: " + state.scrollPercentage)
                    handleScrolling(true, state.scrollPercentage)
                }
            })

            handleSpannedModeSelection(view, webView)
        }

        return view
    }

    private fun handleScrolling (observing: Boolean, int: Int) {
        if (!rangeFound) {
            if (scrollView.scrollY > 100) {
                scrollRange = scrollView.scrollY
                rangeFound = true
            } else {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
        else {
            if (observing) {
                scrollingBuffer = 0

                val y = (scrollRange * int) / 100
                scrollView.scrollTo(scrollView.scrollX, y)
            }
            else {
                if (scrollingBuffer >= 2) {
                    val percentage = (int * 100) / scrollRange
                    scrollVM.setScroll("Preview", percentage)
                }
                else {
                    scrollingBuffer++
                }
            }
        }
    }

    private fun handleSpannedModeSelection(view: View, webView: WebView) {
        activity?.let { activity ->
            if(ScreenHelper.isDualMode(activity)) {
                webVM.getText().observe(requireActivity(), Observer { str ->
                    webView.loadData(str, "text/html", "UTF-8")
                })
            }
        }
    }
}
