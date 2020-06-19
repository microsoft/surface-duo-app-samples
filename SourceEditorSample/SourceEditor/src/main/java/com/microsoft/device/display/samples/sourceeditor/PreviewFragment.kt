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
    // Defines //
    private val DEFAULT_RANGE = 1
    private val MIN_RANGE_THRESHOLD = 100
    private val EMPTY_BUFFER_SIZE = 0
    private val DEFAULT_BUFFER_SIZE = 2

    // Variables //
    private lateinit var scrollView: ScrollView
    private lateinit var scrollVM: ScrollViewModel
    private lateinit var webVM: WebViewModel

    private var scrollingBuffer : Int = DEFAULT_BUFFER_SIZE
    private var scrollRange : Int = DEFAULT_RANGE
    private var rangeFound : Boolean = false

    // initialize fragment elements when view is created
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
            // initialize ViewModels (find existing or create a new one)
            scrollVM = ViewModelProvider(requireActivity()).get(ScrollViewModel::class.java)
            webVM = ViewModelProvider(requireActivity()).get(WebViewModel::class.java)

            val str : String? = (webVM.getText().value)
            webView.loadData(str, "text/html", "UTF-8")

            // set event and data listeners
            scrollView = view.findViewById(R.id.scrollview_preview)
            scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                handleScrolling(false, scrollY)
            }

            scrollVM.getScroll().observe(requireActivity(), Observer { state ->
                if(!state.scrollKey.equals("Preview")) {
                    handleScrolling(true, state.scrollPercentage)
                }
            })

            handleSpannedModeSelection(view, webView)
        }

        return view
    }

    // mirror scrolling logic
    private fun handleScrolling (observing: Boolean, int: Int) {
        // scrolling window has not been calibrated yet
        if (!rangeFound) {
            if (scrollView.scrollY > MIN_RANGE_THRESHOLD) {
                scrollRange = scrollView.scrollY  // successfully calibrated
                rangeFound = true
            } else {
                scrollView.fullScroll(View.FOCUS_DOWN)  // find lower bound
            }
        }
        // scrolling window has been calibrated
        else {
            // code window scrolled, auto scroll to match editor
            if (observing) {
                scrollingBuffer = EMPTY_BUFFER_SIZE

                val y = (scrollRange * int) / 100
                scrollView.scrollTo(scrollView.scrollX, y)
            }
            else {
                // user dragged window to trigger scroll
                if (scrollingBuffer >= DEFAULT_BUFFER_SIZE) {
                    val percentage = (int * 100) / scrollRange
                    scrollVM.setScroll("Preview", percentage)
                }
                // filter out scrolling events caused by auto scrolling
                else {
                    scrollingBuffer++
                }
            }
        }
    }

    private fun handleSpannedModeSelection(view: View, webView: WebView) {
        activity?.let { activity ->
            if(ScreenHelper.isDualMode(activity)) {
                // listen for changes made to the editor
                webVM.getText().observe(requireActivity(), Observer { str ->
                    webView.loadData(str, "text/html", "UTF-8")
                })
            }
        }
    }
}
