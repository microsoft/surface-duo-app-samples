package com.microsoft.device.display.samples.twonote.model

import android.content.res.Resources
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Note {
    var title: String = Resources.getSystem().getString(android.R.string.untitled)
    var text: String = ""
    var dateModified: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return StringBuilder().apply {
            append(title)
            append(" - ")
            append(dateModified.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)))
        }.toString()
    }
}