package com.microsoft.device.display.samples.twonote.model

import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Note {
    var title: String = "Untitled"
    var text: String? = null
    var dateModified: String? = null
        get() {
            return LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        }

    override fun toString(): String {
        return StringBuilder().apply {
            append(title)
            append('\n')
            append(dateModified)
        }.toString()
    }
}