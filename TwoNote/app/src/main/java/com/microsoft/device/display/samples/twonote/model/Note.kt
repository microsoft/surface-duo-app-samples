package com.microsoft.device.display.samples.twonote.model

import android.content.res.Resources
import com.microsoft.device.display.samples.twonote.SerializedStroke
import java.io.Serializable
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Note(var id: Int): Serializable  {
    var title: String = "Note " + id
    var text: String = ""
    var dateModified: LocalDateTime = LocalDateTime.now()
    var drawings: List<SerializedStroke> = listOf()

    override fun toString(): String {
        return StringBuilder().apply {
            append(title)
            append(" - ")
            append(dateModified.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)))
        }.toString()
    }
}