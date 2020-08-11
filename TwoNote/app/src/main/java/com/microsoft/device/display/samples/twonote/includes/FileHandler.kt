/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.includes

import android.app.Activity
import android.net.Uri
import com.google.android.material.textfield.TextInputEditText
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset

/* Class used to make file read/write requests */
class FileHandler(private val activity: Activity) {

    // read text from file specified in uri path
    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        activity.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    // overwrite text from file specified in uri path
    fun alterDocument(uri: Uri, textField: TextInputEditText) {
        try {
            activity.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { stream ->
                    val charset: Charset = Charsets.UTF_8
                    stream.write(
                        textField.getText().toString()
                            .toByteArray(charset)
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // format text for readability (newline chars are dropped in saving/grabbing process)
    fun processTextFileData(uri: Uri, textField: TextInputEditText) {
        val str: String = readTextFromUri(uri)

        val builder = StringBuilder()
        var initHeader = true

        val lines = str.split("<")
        lines.forEach {
            if (initHeader) {
                builder.append(it)
                initHeader = false
            } else {
                builder.append("<" + it + System.getProperty("line.separator"))
            }
        }
        textField.setText(builder.toString())
    }
}