package com.microsoft.device.display.samples.widget.feed

import retrofit2.Call

interface BaseSimpleApi {
    fun rssFeed(): Call<String>
}
