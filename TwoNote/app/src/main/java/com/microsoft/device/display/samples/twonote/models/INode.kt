/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.models

import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class INode(
    var title: String,
    var dateModified: LocalDateTime = LocalDateTime.now(),
    val descriptor: String = "/n",
    var id: Int = 1
) : Serializable {

    fun dateModifiedString(): String {
        return dateModified.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
    }

    override fun toString(): String {
        return title
    }
}
