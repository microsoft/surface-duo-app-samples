/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.sourceeditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button


import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.microsoft.device.display.samples.sourceeditor.viewmodel.WebViewModel

import com.microsoft.device.dualscreen.layout.ScreenHelper

class PreviewFragment : Fragment() {
    private lateinit var viewModel: WebViewModel
    private lateinit var codeButton: Button

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
            viewModel = ViewModelProvider(requireActivity()).get(WebViewModel::class.java)

            val str : String? = (viewModel.getText().value)
            //Log.d("VMDPREVIEW", "received text: " + str)
            webView.loadData(str, "text/html", "UTF-8")

            handleSpannedModeSelection(view, webView)
        }

        return view
    }

    private fun handleSpannedModeSelection(view: View, webView: WebView) {
        activity?.let { activity ->
            if(ScreenHelper.isDualMode(activity)) {
                viewModel.getText().observe(requireActivity(), Observer {str ->
                    webView.loadData(str, "text/html", "UTF-8")
                })
            }
            else {
                codeButton = view.findViewById(R.id.btn_switch_to_code)
                setOnClickListenerForPreviewView(codeButton)
            }
        }

    }

    private fun setOnClickListenerForPreviewView(codeBtn: Button){
        codeBtn.setOnClickListener {
            startCodeFragment()
        }
    }

    private fun startCodeFragment() {
        parentFragmentManager.beginTransaction()
                .replace(
                        R.id.single_screen_container_id,
                        CodeFragment(),
                        null
                ).addToBackStack(null)
                .commit()
    }
}
