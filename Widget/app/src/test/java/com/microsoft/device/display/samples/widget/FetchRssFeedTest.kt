package com.microsoft.device.display.samples.widget

import android.content.Context
import com.microsoft.device.display.samples.widget.feed.BaseSimpleApi
import com.microsoft.device.display.samples.widget.feed.RssFeed
import com.microsoft.device.display.samples.widget.feed.RssFeed.REQUEST_HEADER_VALUE
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Call
import retrofit2.http.GET

class FetchRssFeedTest {

    private val mockContext = mockkClass(Context::class)
    private val mockWebServer = MockWebServer()

    init {
        mockkObject(RssFeed)
    }

    @After
    fun resetMocks() {
        clearAllMocks()
    }

    @Test
    fun fetchRssFeed_shouldCallParseXmlWithResponse_whenIsSuccessful() {
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <channel>\n" +
                "        <title>Surface Duo Blog</title>\n" +
                "    </channel>" +
                "</rss>"

        val successResponse = MockResponse().setBody(xmlString)

        every {
            RssFeed.getFeedUrlFromPreferences(mockContext)
        } returns mockWebServer.url("/").toString()
        every { RssFeed.parseXml(xmlString) } returns(emptyList())

        mockWebServer.enqueue(successResponse)

        RssFeed.fetchRssFeed(mockContext, TestApi::class.java)

        mockWebServer.takeRequest()

        verify(exactly = 1) { RssFeed.parseXml(xmlString) }
    }

    @Test
    fun fetchRssFeed_shouldUseCorrectHeaders() {
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <channel>\n" +
                "        <title>Surface Duo Blog</title>\n" +
                "    </channel>" +
                "</rss>"

        val successResponse = MockResponse().setBody(xmlString)

        every {
            RssFeed.getFeedUrlFromPreferences(mockContext)
        } returns mockWebServer.url("/").toString()
        every { RssFeed.parseXml(xmlString) } returns(emptyList())

        mockWebServer.enqueue(successResponse)

        RssFeed.fetchRssFeed(mockContext, TestApi::class.java)

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

    private interface TestApi : BaseSimpleApi {
        @GET("/test")
        override fun rssFeed(): Call<String>
    }
}
