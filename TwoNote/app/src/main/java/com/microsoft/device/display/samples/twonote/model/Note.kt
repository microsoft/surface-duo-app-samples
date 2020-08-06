/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.model

import com.microsoft.device.display.samples.twonote.SerializedImage
import com.microsoft.device.display.samples.twonote.SerializedStroke
import java.io.Serializable

class Note(var id: Int) : Serializable {
    var title: String = "Note $id"
    var text: String = ""
    var drawings: List<SerializedStroke> = listOf()
    var images: List<SerializedImage> = listOf()
}
