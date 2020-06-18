/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.sourceeditor

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.device.display.samples.sourceeditor.viewmodel.ScrollViewModel
import com.microsoft.device.display.samples.sourceeditor.viewmodel.WebViewModel
import com.microsoft.device.dualscreen.layout.ScreenHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class CodeFragment : Fragment() {
    private lateinit var webVM: WebViewModel
    private lateinit var scrollVM: ScrollViewModel
    private lateinit var previewBtn : Button
    private lateinit var textField : TextInputEditText
    private lateinit var scrollView : ScrollView

    private var scrollRange : Int = 1
    private var rangeFound : Boolean = false
    private var scrollingBuffer : Int = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_code, container, false)

        activity?.let {
            webVM = ViewModelProvider(requireActivity()).get(WebViewModel::class.java)
            scrollVM = ViewModelProvider(requireActivity()).get(ScrollViewModel::class.java)

            textField = view.findViewById(R.id.textinput_code)
            scrollView = view.findViewById(R.id.scrollview_code)

            if (webVM.getText().value == null) {
                webVM.setText(readFile("index.html", context))
            }

            textField.setText(webVM.getText().value)

            scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                Log.d("VMDCODE", "Scrolling")
                handleScrolling(false, scrollY)
            }

            scrollVM.getScroll().observe(requireActivity(), Observer { state ->
                Log.d("VMDCODE", "data received")
                if(!state.scrollKey.equals("Code")) {
                    handleScrolling(true, state.scrollPercentage)
                }
            })

            setOnChangeListenerForTextInput(textField)
            handleSpannedModeSelection(view)
        }

        return view
    }

    private fun readFile(file : String, context : Context?) : String {
        return BufferedReader(InputStreamReader(context?.assets?.open(file))).useLines { lines ->
            val results = StringBuilder()
            lines.forEach { results.append(it + "\n") }
            results.toString()
        }
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
                    scrollVM.setScroll("Code", percentage)
                }
                else {
                    scrollingBuffer++
                }
            }
        }
    }

    private fun setOnClickListenerForCodeView(previewBtn: Button){
        previewBtn.setOnClickListener {
            startPreviewFragment()
        }
    }

    private fun setOnChangeListenerForTextInput(field: TextInputEditText) {
        field.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("VMDCode", "textChanged")
                webVM.setText(field.text.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun startPreviewFragment() {
        parentFragmentManager.beginTransaction()
            .replace(
                    R.id.single_screen_container_id,
                    PreviewFragment(),
                    null
            ).addToBackStack(null)
            .commit()
    }

    private fun handleSpannedModeSelection(view: View) {
        activity?.let {
            previewBtn = view.findViewById(R.id.btn_switch_to_preview)
            if (ScreenHelper.isDualMode(it)) {
                previewBtn.visibility = View.INVISIBLE
                parentFragmentManager
                        .beginTransaction()
                        .replace(
                                R.id.dual_screen_end_container_id,
                                PreviewFragment(), null
                        )
                        .commit()
            }
            else {
                previewBtn.visibility = View.VISIBLE
                setOnClickListenerForCodeView(previewBtn)
            }
        }
    }
}
