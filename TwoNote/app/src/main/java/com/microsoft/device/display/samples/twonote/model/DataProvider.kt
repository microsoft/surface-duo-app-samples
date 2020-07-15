/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.model

import java.util.ArrayList

object DataProvider {
    val notes: ArrayList<Note> = ArrayList()

    fun createNote(title: String) {
        // Add first (most recently edited)
        notes.add(0, Note(title))
    }
}
