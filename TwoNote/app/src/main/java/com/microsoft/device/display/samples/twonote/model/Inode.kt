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

class Inode(
    var title: String,
    var dateModified: LocalDateTime,
    var id: Int
) : Serializable {

    override fun toString(): String {
        return StringBuilder().apply {
            append(title)
            append(" - ")
            append(dateModified.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)))
        }.toString()
    }
}
