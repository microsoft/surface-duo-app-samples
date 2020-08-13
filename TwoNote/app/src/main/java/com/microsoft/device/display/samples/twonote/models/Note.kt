/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.models

import java.io.Serializable

class Note(var id: Int, localizedTitle: String) : Serializable {
    var title: String = "$localizedTitle $id"
    var text: String = ""
    var drawings: List<SerializedStroke> = listOf()
    var images: List<SerializedImage> = listOf()
}
