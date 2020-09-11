/*
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.widget

import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.microsoft.device.display.samples.widget.feed.RssFeed
import com.microsoft.device.display.samples.widget.feed.RssFeed.CHANNEL
import com.microsoft.device.display.samples.widget.feed.RssFeed.CREATOR
import com.microsoft.device.display.samples.widget.feed.RssFeed.DESCRIPTION
import com.microsoft.device.display.samples.widget.feed.RssFeed.ITEM
import com.microsoft.device.display.samples.widget.feed.RssFeed.LINK
import com.microsoft.device.display.samples.widget.feed.RssFeed.PUB_DATE
import com.microsoft.device.display.samples.widget.feed.RssFeed.TITLE
import com.microsoft.device.display.samples.widget.feed.RssItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.`is` as iz

@RunWith(AndroidJUnit4ClassRunner::class)
@MediumTest
class XmlParserRssFeedTest {

    @Test
    fun parseXml_shouldReturnEmptyList_whenNoRssItemsFound() {
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL></$CHANNEL>" +
                "</rss>"

        val rssItems = RssFeed.parseXml(xmlString)
        assertThat(
            "Method should return empty list if item tag is not found in xml",
            rssItems,
            iz(emptyList())
        )
    }

    @Test
    fun parseXml_shouldReturnOneElementList_whenOneRssItemFound() {
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "        <$ITEM></$ITEM>\n" +
                "    </$CHANNEL>" +
                "</rss>"

        val emptyRssItem = RssItem(null, null, null, null, null)
        val rssItems = RssFeed.parseXml(xmlString)

        assertEquals(
            "Method should return list containing one element if one item tag is found",
            1,
            rssItems.size
        )

        assertEquals(
            "Rss Item should be empty when only item tag is encountered",
            emptyRssItem,
            rssItems[0]
        )
    }

    @Test
    fun parseXml_shouldReturnEmptyList_whenItemTagIsLonger() {
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "        <$ITEM$ITEM></$ITEM$ITEM>\n" +
                "    </$CHANNEL>" +
                "</rss>"

        val rssItems = RssFeed.parseXml(xmlString)
        assertThat(
            "Method should return empty list if item tag name in xml is longer",
            rssItems,
            iz(emptyList())
        )
    }

    @Test
    fun parseXml_shouldContainElementWithTitle_whenItemHasTitle() {
        val expectedTitle = "Build and test dual-screen web apps"
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "        <$ITEM>\n" +
                "            <$TITLE>$expectedTitle</$TITLE>\n" +
                "        </$ITEM>\n" +
                "    </$CHANNEL>" +
                "</rss>"

        val rssItem = RssFeed.parseXml(xmlString)[0]

        assertNotNull(
            "Rss Item should not have null title",
            rssItem?.title
        )

        assertEquals(
            "Rss Item should have title = $expectedTitle",
            expectedTitle,
            rssItem?.title
        )
    }

    @Test
    fun parseXml_shouldContainElementWithCreator_whenItemHasCreator() {
        val expectedCreator = "Craig Dunn"
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "        <$ITEM>\n" +
                "            <$CREATOR><![CDATA[$expectedCreator]]></$CREATOR>\n" +
                "        </$ITEM>\n" +
                "    </$CHANNEL>" +
                "</rss>"

        val rssItem = RssFeed.parseXml(xmlString)[0]

        assertNotNull(
            "Rss Item should not have null creator",
            rssItem?.creator
        )

        assertEquals(
            "Rss Item should have creator = $expectedCreator",
            expectedCreator,
            rssItem?.creator
        )
    }

    @Test
    fun parseXml_shouldContainElementWithDate_whenItemHasDate() {
        val expectedDate = "Thu, 03 Sep 2020 21:17:19"
        val actualDate = "Thu, 03 Sep 2020 21:17:19 +0000"
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "        <$ITEM>\n" +
                "            <$PUB_DATE>$actualDate</$PUB_DATE>\n" +
                "        </$ITEM>\n" +
                "    </$CHANNEL>" +
                "</rss>"

        val rssItem = RssFeed.parseXml(xmlString)[0]

        assertNotNull(
            "Rss Item should not have null date",
            rssItem?.date
        )

        assertEquals(
            "Rss Item should have date = $expectedDate",
            expectedDate,
            rssItem?.date
        )
    }

    @Test
    fun parseXml_shouldContainElementWithLink_whenItemHasLink() {
        val expectedLink = "https://devblogs.microsoft.com/surface-duo/build-and-test-dual-screen-web-apps/"
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "        <$ITEM>\n" +
                "            <$LINK>$expectedLink</$LINK>\n" +
                "        </$ITEM>\n" +
                "    </$CHANNEL>" +
                "</rss>"

        val rssItem = RssFeed.parseXml(xmlString)[0]

        assertNotNull(
            "Rss Item should not have null href",
            rssItem?.href
        )

        assertEquals(
            "Rss Item should have href = $expectedLink",
            expectedLink,
            rssItem?.href
        )
    }

    @Test
    fun parseXml_shouldContainElementWithDescription_whenItemHasShortDescription() {
        val actualDescription = "Hello dual-screen web developers! This is the day"
        val expectedDescription = "Hello dual-screen web developers! This is the day..."
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "        <$ITEM>\n" +
                "            <$DESCRIPTION>$actualDescription</$DESCRIPTION>\n" +
                "        </$ITEM>\n" +
                "    </$CHANNEL>" +
                "</rss>"

        val rssItem = RssFeed.parseXml(xmlString)[0]

        assertNotNull(
            "Rss Item should not have null body",
            rssItem?.body
        )

        assertEquals(
            "Rss Item should have body = $expectedDescription",
            expectedDescription,
            rssItem?.body
        )
    }

    @Test
    fun parseXml_shouldContainElementWithDescription_whenItemHasLongDescription() {
        val builder = StringBuilder()
        for (index in 0 until RssFeed.DESCRIPTION_MAX_CHARS) {
            builder.append("a")
        }
        val expectedDescription = builder.append("...").toString()
        val actualDescription = expectedDescription + "description"

        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <$CHANNEL>\n" +
                "        <$ITEM>\n" +
                "            <$DESCRIPTION>$actualDescription</$DESCRIPTION>\n" +
                "        </$ITEM>\n" +
                "    </$CHANNEL>" +
                "</rss>"

        val rssItem = RssFeed.parseXml(xmlString)[0]

        assertNotNull(
            "Rss Item should not have null body",
            rssItem?.body
        )

        assertEquals(
            "Rss Item should have body = $expectedDescription",
            expectedDescription,
            rssItem?.body
        )
    }
}
