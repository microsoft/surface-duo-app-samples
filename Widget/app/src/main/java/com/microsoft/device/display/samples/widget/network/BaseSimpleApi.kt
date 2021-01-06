/*
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.widget.network

import retrofit2.Call

interface BaseSimpleApi {
    fun rssFeed(): Call<String>
}
