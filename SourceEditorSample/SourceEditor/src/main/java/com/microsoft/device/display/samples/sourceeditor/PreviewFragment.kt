/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.sourceeditor

import Defines
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ScrollView

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.microsoft.device.display.samples.sourceeditor.viewmodel.ScrollViewModel
import com.microsoft.device.display.samples.sourceeditor.viewmodel.WebViewModel
import com.microsoft.device.dualscreen.layout.ScreenHelper

/* Fragment that defines functionality for the source code previewer */
class PreviewFragment : Fragment() {
    private lateinit var scrollView: ScrollView
    private lateinit var scrollVM: ScrollViewModel
    private lateinit var webVM: WebViewModel

    private var scrollingBuffer : Int = Defines.DEFAULT_BUFFER_SIZE
    private var scrollRange : Int = Defines.DEFAULT_RANGE
    private var rangeFound : Boolean = false

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
            webView.loadData(str, Defines.HTML_TYPE, Defines.ENCODING)

            handleSpannedModeSelection(view, webView)
        }

        return view
    }

    // mirror scrolling logic
    private fun handleScrolling (observing: Boolean, int: Int) {
        if (!rangeFound) {
            calibrateScrollView()
        }
        else {
            // code window scrolled, auto scroll to match editor
            if (observing) {
                autoScroll(int)
            } else {
                updateScrollValues(int, Defines.PREVIEW_KEY)
            }
        }
    }

    private fun handleSpannedModeSelection(view: View, webView: WebView) {
        activity?.let { activity ->
            if (ScreenHelper.isDualMode(activity)) {
                scrollingBuffer = Defines.DEFAULT_BUFFER_SIZE
                scrollRange = Defines.DEFAULT_RANGE
                rangeFound = false

                // set event and data listeners
                scrollView = view.findViewById(R.id.scrollview_preview)
                scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                    handleScrolling(false, scrollY)
                }

                scrollVM.getScroll().observe(requireActivity(), Observer { state ->
                    if (!state.scrollKey.equals(Defines.PREVIEW_KEY)) {
                        handleScrolling(true, state.scrollPercentage)
                    }
                })

                // listen for changes made to the editor
                webVM.getText().observe(requireActivity(), Observer { str ->
                    webView.loadData(str, Defines.HTML_TYPE, Defines.ENCODING)
                })
            }
        }
    }

    // get bounds of scroll window
    private fun calibrateScrollView() {
        if (scrollView.scrollY > Defines.MIN_RANGE_THRESHOLD) {
            scrollRange = scrollView.scrollY  // successfully calibrated
            rangeFound = true
        } else {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    // mirror scrolling events triggered on preview window
    private fun autoScroll(int: Int) {
        scrollingBuffer = Defines.EMPTY_BUFFER_SIZE

        val y = (scrollRange * int) / 100
        scrollView.scrollTo(scrollView.scrollX, y)
    }

    // handle scroll events triggered on editor window
    private fun updateScrollValues(int: Int, str: String) {
        if (scrollingBuffer >= Defines.DEFAULT_BUFFER_SIZE) {
            val percentage = (int * 100) / scrollRange
            scrollVM.setScroll(str, percentage)
        } else {
            // filter out scrolling events caused by auto scrolling
            scrollingBuffer++
        }
    }
}
