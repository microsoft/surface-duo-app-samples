package com.microsoft.device.display.samples.widget

import android.text.Html
import android.text.SpannableString
import com.microsoft.device.display.samples.widget.feed.RssFeed
import com.microsoft.device.display.samples.widget.feed.RssItem
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParserRssFeedTest {

    init {
        mockkObject(RssFeed)
    }

    @After
    fun resetMocks() {
        clearAllMocks()
    }

    @Test
    fun shouldInsertItemInRssItems_shouldReturnTrue_whenTagIsLowercaseAndItemNotNull() {
        val mockTag = RssFeed.ITEM
        val mockRssItem = RssItem(null, null, null, null, null)

        assertTrue(
            "Method should return true if item tag is lowercase",
            RssFeed.shouldInsertItemInRssItems(mockTag, mockRssItem)
        )
    }

    @Test
    fun shouldInsertItemInRssItems_shouldReturnTrue_whenTagIsUppercaseAndItemNotNull() {
        val mockTag = RssFeed.ITEM.toUpperCase()
        val mockRssItem = RssItem(null, null, null, null, null)

        assertTrue(
            "Method should return true if item tag is uppercase",
            RssFeed.shouldInsertItemInRssItems(mockTag, mockRssItem)
        )
    }

    @Test
    fun shouldInsertItemInRssItems_shouldReturnFalse_whenItemIsNull() {
        val mockTag = RssFeed.ITEM

        assertFalse(
            "Method should return false if Rss Item is null",
            RssFeed.shouldInsertItemInRssItems(mockTag, null)
        )
    }

    @Test
    fun shouldInsertItemInRssItems_shouldReturnFalse_whenTagIsLonger() {
        val mockTag = RssFeed.ITEM + RssFeed.ITEM
        val mockRssItem = RssItem(null, null, null, null, null)

        assertFalse(
            "Method should return false if tag name contains more than item",
            RssFeed.shouldInsertItemInRssItems(mockTag, mockRssItem)
        )
    }

    @Test
    fun isEndOfFeed_shouldReturnTrue_whenTagIsLowercase() {
        val mockTag = RssFeed.CHANNEL

        assertTrue(
            "Method should return true if channel tag is lowercase",
            RssFeed.isEndOfFeed(mockTag)
        )
    }

    @Test
    fun isEndOfFeed_shouldReturnTrue_whenTagIsUppercase() {
        val mockTag = RssFeed.CHANNEL.toUpperCase()

        assertTrue(
            "Method should return true if channel tag is uppercase",
            RssFeed.isEndOfFeed(mockTag)
        )
    }

    @Test
    fun isEndOfFeed_shouldReturnTrue_whenTagIsLonger() {
        val mockTag = RssFeed.CHANNEL + RssFeed.CHANNEL

        assertFalse(
            "Method should return false if tag name contains more than channel",
            RssFeed.isEndOfFeed(mockTag)
        )
    }

    @Test
    fun parseDate_shouldReturnShortDate() {
        val mockDate = "Tue, 01 Sep 2020 20:00:04 +0000"
        val expectedDate = "Tue, 01 Sep 2020 20:00:04"

        assertEquals(
            "Method should return date without timezone",
            expectedDate,
            RssFeed.parseDate(mockDate)
        )
    }

    @Test
    fun parseTitle_shouldCallStripHtml() {
        val mockTitle = "title"
        every { RssFeed.stripHtml(mockTitle) }.returns(mockTitle)

        RssFeed.parseTitle(mockTitle)

        verify(exactly = 1) { RssFeed.stripHtml(mockTitle) }
    }

    @Test
    fun parseDescription_shouldUseCutDescription_whenParameterIsLongerThanLimit() {
        val builder = StringBuilder()
        for (index in 0 until RssFeed.DESCRIPTION_MAX_CHARS) {
            builder.append("a")
        }
        val result = builder.append("...").toString()
        val mockLongDescription = result + "description"

        every { RssFeed.stripHtml(result) }.returns(result)

        RssFeed.parseDescription(mockLongDescription)

        verify(exactly = 1) { RssFeed.stripHtml(result) }
    }

    @Test
    fun parseDescription_shouldUseParameter_whenParameterIsShorterThanLimit() {
        val mockShortDescription = "description"
        val expectedDescription = "description..."

        every { RssFeed.stripHtml(expectedDescription) }.returns(expectedDescription)

        RssFeed.parseDescription(mockShortDescription)

        verify(exactly = 1) { RssFeed.stripHtml(expectedDescription) }
    }

    @Test
    fun stripHtml_shouldCallFromHtml() {
        val expectedCleanText = "html"
        val mockText = "*&h..t??m*l;"
        val mockHtml = "<p>$mockText</br><p>"
        val spannableText = mockk<SpannableString>(mockText)

        mockkStatic("android.text.Html")

        every {
            Html.fromHtml(mockHtml, Html.FROM_HTML_MODE_COMPACT)
        } returns(spannableText)

        every {
            spannableText.toString()
        } returns(mockText)

        assertEquals(
            "Method should remove special characters",
            expectedCleanText,
            RssFeed.stripHtml(mockHtml)
        )

        verify(exactly = 1) { Html.fromHtml(mockHtml, Html.FROM_HTML_MODE_COMPACT) }
    }
}
