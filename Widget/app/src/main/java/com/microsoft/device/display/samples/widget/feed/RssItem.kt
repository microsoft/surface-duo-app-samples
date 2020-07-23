/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */
package com.microsoft.device.display.samples.widget.feed

data class RssItem(
    var title: String?,
    var creator: String?,
    var date: String?,
    var body: String?,
    var href: String?
)