/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.sourceeditor.includes

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.app.ActivityCompat.startActivityForResult
import com.microsoft.device.display.samples.sourceeditor.viewmodel.WebViewModel
import java.io.*
import java.nio.charset.Charset

/* Class used to make file read/write requests */
class FileHandler (val activity: Activity,
                   val webVM: WebViewModel,
                   val contentResolver: ContentResolver) {

    // intent request codes
    val CREATE_FILE = 1
    val PICK_TXT_FILE = 2

    // create a window prompting user to save a new file
    // defaults to public Downloads folder if uri is empty
    fun createFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "code")

            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(activity, intent, CREATE_FILE, null)
    }

    // creating a window prompting user to choose a file to open
    // defaults to public Downloads folder if uri is empty
    fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"

            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(activity, intent, PICK_TXT_FILE, null)
    }

    // read text from file specified in uri path
    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
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
    fun alterDocument(uri: Uri) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    val charset: Charset = Charsets.UTF_8
                    it.write(
                            webVM.getText().value.toString()
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
    fun processFileData(uri: Uri) {
        val str: String = readTextFromUri(uri)

        val builder = StringBuilder()
        var initHeader = true

        val lines = str.split("<")
        lines.forEach{
            if (initHeader) {
                builder.append(it)
                initHeader = false
            } else {
                builder.append("<" + it + System.getProperty("line.separator"))
            }
        }
        webVM.setText(builder.toString())
    }
}