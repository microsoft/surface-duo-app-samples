/*
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.widget

import com.microsoft.device.display.samples.widget.feed.RssFeed.CHANNEL
import com.microsoft.device.display.samples.widget.feed.RssFeed.ITEM
import com.microsoft.device.display.samples.widget.feed.RssFeed.REQUEST_HEADER_VALUE
import com.microsoft.device.display.samples.widget.feed.RssFeed.TITLE
import com.microsoft.device.display.samples.widget.network.BaseSimpleApi
import com.microsoft.device.display.samples.widget.network.NetworkFeed
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Call
import retrofit2.http.GET

class NetworkFeedTest {
    private val mockWebServer = MockWebServer()

    @Test
    fun fetchRssFeed_shouldUseCorrectRequestHeaders() {
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "        <title>Surface Duo Blog</title>\n" +
                "    </$CHANNEL>" +
                "</rss>"

        val successResponse = MockResponse().setBody(xmlString)

        mockWebServer.enqueue(successResponse)

        val baseUrl = mockWebServer.url("/").toString()

        NetworkFeed.fetchRssFeed(baseUrl, TestApi::class.java)

        val request = mockWebServer.takeRequest()

        assertEquals(
            "Content-Type Header should have the declared value",
            REQUEST_HEADER_VALUE,
            request.getHeader("Content-Type")
        )
        assertEquals(
            "Accept Header should have the declared value",
            REQUEST_HEADER_VALUE,
            request.getHeader("Accept")
        )
    }

    @Test
    fun fetchRssFeed_shouldReturnResponseBody_whenRequestIsSuccessful() {
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "       <$ITEM>\n" +
                "           <$TITLE>Surface Duo Blog</$TITLE>\n" +
                "       </$ITEM>" +
                "    </$CHANNEL>" +
                "</rss>"

        val successResponse = MockResponse().setBody(xmlString)

        mockWebServer.enqueue(successResponse)

        val baseUrl = mockWebServer.url("/").toString()

        val response = NetworkFeed.fetchRssFeed(baseUrl, TestApi::class.java)

        mockWebServer.takeRequest()

        assertEquals(
            "The method should return the response body when request is successful",
            xmlString,
            response
        )
    }

    @Test
    fun fetchRssFeed_shouldReturnNull_whenExceptionAppears() {
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        val baseUrl = mockWebServer.url("/").toString()

        val response = NetworkFeed.fetchRssFeed(baseUrl, TestApi::class.java)

        mockWebServer.takeRequest()

        assertNull(
            "The method should return null when request is not successful",
            response
        )
    }

    private interface TestApi : BaseSimpleApi {
        @GET("/test")
        override fun rssFeed(): Call<String>
    }
}
