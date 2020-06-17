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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.device.display.samples.sourceeditor.viewmodel.WebViewModel
import com.microsoft.device.dualscreen.layout.ScreenHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class CodeFragment : Fragment() {
    private lateinit var viewModel: WebViewModel
    private lateinit var previewBtn : Button
    private lateinit var textField : TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_code, container, false)

        activity?.let {
            viewModel = ViewModelProvider(requireActivity()).get(WebViewModel::class.java)
            textField = view.findViewById(R.id.textInputField)

            if (viewModel.getText().value == null) {
                viewModel.setText(readFile("index.html", context))
            }

            textField.setText(viewModel.getText().value)
            setOnChangeListenerForTextInput(textField)

            textField.setPaddingRelative(textField.paddingStart, textField.paddingTop,
                    textField.paddingEnd, textField.paddingBottom + 50
            )


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

    private fun getTextFromWeb(urlString: String, textField: TextInputEditText, view: View) {
        Thread(Runnable {
            try {
                val url = URL(urlString)
                val con = url.openConnection() as HttpsURLConnection

                val datas = con.inputStream.bufferedReader().readText()
                Log.d("VMDCODE HTML content", datas)
                view.post{viewModel.setText(datas)}
                view.post{textField.setText(datas)}

            } catch (ex: Exception) {
                Log.d("Exception", ex.toString())
            }
        }).start()
    }

    private fun setOnClickListenerForCodeView(previewBtn: Button){
        previewBtn.setOnClickListener {
            startPreviewFragment()
        }
    }

    private fun setOnChangeListenerForTextInput(field: TextInputEditText) {
        field.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("VMDCode", "before text hit: " +
                parentFragmentManager.fragments.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("VMDCode", "textChanged")
                viewModel.setText(field.text.toString())
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
            if (ScreenHelper.isDualMode(it)) {
                parentFragmentManager
                        .beginTransaction()
                        .replace(
                                R.id.dual_screen_end_container_id,
                                PreviewFragment(), null
                        )
                        .commit()
            }
            else {
                previewBtn = view.findViewById(R.id.btn_switch_to_preview)
                setOnClickListenerForCodeView(previewBtn)
            }
        }
    }
}
