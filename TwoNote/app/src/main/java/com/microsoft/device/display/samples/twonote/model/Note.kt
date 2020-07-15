package com.microsoft.device.display.samples.twonote.model

import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Note(title: String) {
    var title: String = title
    var text: String? = null
    val dateModified: String
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