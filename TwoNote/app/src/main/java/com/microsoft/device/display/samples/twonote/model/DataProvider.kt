/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.model

import java.util.ArrayList

object DataProvider {
    val notes: ArrayList<Note> = ArrayList()

    /**
     * Create new note and add it to the top of the note list (position 0)
     */
    fun createNote(index: Int): Int{
        notes.add(Note(index))
        return notes.lastIndex
    }
    fun addNote(note: Note) {
        notes.add(note)
    }
    fun clear() {
        for(n in notes.lastIndex downTo 0) {
            notes.removeAt(n)
        }
    }
}
