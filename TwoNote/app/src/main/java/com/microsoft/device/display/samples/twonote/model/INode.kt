/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.model

import java.io.Serializable
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class INode(
    var title: String = "Note 0",
    var dateModified: LocalDateTime = LocalDateTime.now(),
    val descriptor: String = "/n",
    var id: Int = 0
) : Serializable {

    fun dateModifiedString(): String {
        return dateModified.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
    }

    // REVISIT: this is no longer used for ListView display, so should we still keep it?
    override fun toString(): String {
        return StringBuilder().apply {
            append(title)
            append(" - ")
            append(dateModified.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)))
        }.toString()
    }
}
