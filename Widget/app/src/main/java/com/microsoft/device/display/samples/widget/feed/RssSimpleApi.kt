/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */
package com.microsoft.device.display.samples.widget.feed

import retrofit2.Call
import retrofit2.http.GET

interface RssSimpleApi : BaseSimpleApi {
    @GET(".")
    override fun rssFeed(): Call<String>
}
