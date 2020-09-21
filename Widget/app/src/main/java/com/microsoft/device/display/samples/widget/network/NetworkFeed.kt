/*
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.widget.network

import android.util.Log
import com.microsoft.device.display.samples.widget.feed.RssFeed
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException

object NetworkFeed {

    fun <T : BaseSimpleApi> fetchRssFeed(baseUrl: String, apiClass: Class<T>): String? {
        val retrofit = configureNetworkCall(baseUrl)
        val rssSimpleApi = retrofit.create(apiClass)

        // Making surface duo blog data request synchronous since this call is done
        // from onDataSetChanged in the widget
        val request: Call<String> = rssSimpleApi.rssFeed()
        try {
            val response = request.execute()
            if (response.isSuccessful) {
                return response.body()
            }
        } catch (e: IOException) {
            Log.e(RssFeed.TAG, e.toString())
            return null
        }
        return null
    }

    private fun configureNetworkCall(baseUrl: String): Retrofit {
        val httpClientBuilder = OkHttpClient.Builder()

        // HttpClient interceptor to add specific rss feeds headers
        // Some of the rss sources require these or else will fetch html instead of rss
        httpClientBuilder.addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.header("Content-Type", RssFeed.REQUEST_HEADER_VALUE)
                requestBuilder.header("Accept", RssFeed.REQUEST_HEADER_VALUE)
                return chain.proceed(requestBuilder.build())
            }
        })
        val okHttpClient = httpClientBuilder.build()

        // Retrofit constructor for surface duo blog data request
        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClient).build()
    }
}
