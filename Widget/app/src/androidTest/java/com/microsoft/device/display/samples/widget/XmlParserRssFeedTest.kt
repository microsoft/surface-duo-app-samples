package com.microsoft.device.display.samples.widget

import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.microsoft.device.display.samples.widget.feed.RssFeed
import com.microsoft.device.display.samples.widget.feed.RssItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import org.hamcrest.Matchers.`is` as iz

@RunWith(AndroidJUnit4ClassRunner::class)
@MediumTest
class XmlParserRssFeedTest {

    @Test
    fun parseXml_shouldReturnEmptyList_whenNoRssItemsFound() {
        val xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\">" +
                "    <channel>\n" +
                "        <title>Surface Duo Blog</title>\n" +
                "    </channel>" +
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
                "<rss version=\"2.0\"\n" +
                "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" +
                "    <channel>\n" +
                "        <item>\n" +
                "            <title>Build and test dual-screen web apps</title>\n" +
                "            <link>\n" +
                "                https://devblogs.microsoft.com/surface-duo/build-and-test-dual-screen-web-apps/\n" +
                "            </link>\n" +
                "            <dc:creator>\n" +
                "                <![CDATA[Craig Dunn]]>\n" +
                "            </dc:creator>\n" +
                "            <pubDate>Thu, 03 Sep 2020 21:17:19 +0000</pubDate>\n" +
                "            <description>\n" +
                "                <![CDATA[<p>Hello dual-screen web developers!</p>\n" +
                "                <p>  In a previous blog post, we talked about the \n" +
                "                dual-screen CSS @media primitives and the getWindowSegments() \n" +
                "                API, and how they could be tested with polyfill extensions. \n" +
                "                Now those features are built-in to Microsoft Edge and Chrome™ \n" +
                "                browser canary builds.</p>]]>\n" +
                "            </description>\n" +
                "        </item>\n" +
                "    </channel>\n" +
                "</rss>"

        val rssItems = RssFeed.parseXml(xmlString)
        assertEquals(
            "Method should return list containing one element if one item tag is found",
            1,
            rssItems.size
        )
    }

    @Test
    fun handleStartTagElement_shouldReturnEmptyElement_whenIsStartTag() {
        val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        xmlPullParser.setInput(
            StringReader("<item></item>")
        )
        xmlPullParser.next()

        val emptyRssItem = RssItem(null, null, null, null, null)
        val actualRssItem = RssItem("title", "creator", null, null, null)

        assertEquals(
            "Rss Item should be empty when start tag is encountered",
            emptyRssItem,
            RssFeed.handleStartTagElement(xmlPullParser, actualRssItem)
        )
    }

    @Test
    fun handleStartTagElement_shouldCallParseRequiredElement_whenIsElementTag() {
        val expectedTitle = "This is title"
        val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        xmlPullParser.setInput(
            StringReader("<${RssFeed.TITLE}>$expectedTitle</${RssFeed.TITLE}>")
        )
        xmlPullParser.next()

        val emptyRssItem = RssItem(null, null, null, null, null)
        val expectedRssItem = RssItem(expectedTitle, null, null, null, null)

        assertEquals(
            "Rss Item should have title = $expectedTitle",
            expectedRssItem,
            RssFeed.handleStartTagElement(xmlPullParser, emptyRssItem)
        )
    }

    @Test
    fun parseRequiredElement_shouldExtractCorrectTitle() {
        val expectedTitle = "This is title"
        val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        xmlPullParser.setInput(StringReader("<${RssFeed.TITLE}>$expectedTitle</${RssFeed.TITLE}>"))
        xmlPullParser.next()

        val actualItem = RssItem(null, null, null, null, null)

        assertEquals(
            "Tag Name should be ${RssFeed.TITLE}",
            RssFeed.TITLE,
            xmlPullParser.name
        )

        RssFeed.parseRequiredElement(xmlPullParser, actualItem, RssFeed.TITLE)

        assertEquals(
            "Tag of ${RssFeed.TITLE} should have $expectedTitle value",
            expectedTitle,
            actualItem.title
        )
    }

    @Test
    fun parseRequiredElement_shouldExtractCorrectCreator() {
        val expectedCreator = "Craig Dunn"
        val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        xmlPullParser.setInput(
            StringReader("<${RssFeed.CREATOR}><![CDATA[$expectedCreator]]></${RssFeed.CREATOR}>")
        )
        xmlPullParser.next()

        val actualItem = RssItem(null, null, null, null, null)

        assertEquals(
            "Tag Name should be ${RssFeed.CREATOR}",
            RssFeed.CREATOR,
            xmlPullParser.name
        )

        RssFeed.parseRequiredElement(xmlPullParser, actualItem, RssFeed.CREATOR)

        assertEquals(
            "Tag of ${RssFeed.CREATOR} should have $expectedCreator value",
            expectedCreator,
            actualItem.creator
        )
    }

    @Test
    fun parseRequiredElement_shouldExtractCorrectDate() {
        val expectedDate = "Thu, 03 Sep 2020 21:17:19"
        val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        xmlPullParser.setInput(
            StringReader("<${RssFeed.PUB_DATE}>$expectedDate +0000</${RssFeed.PUB_DATE}>")
        )
        xmlPullParser.next()

        val actualItem = RssItem(null, null, null, null, null)

        assertEquals(
            "Tag Name should be ${RssFeed.PUB_DATE}",
            RssFeed.PUB_DATE,
            xmlPullParser.name
        )

        RssFeed.parseRequiredElement(xmlPullParser, actualItem, RssFeed.PUB_DATE)

        assertEquals(
            "Tag of ${RssFeed.PUB_DATE} should have $expectedDate value",
            expectedDate,
            actualItem.date
        )
    }

    @Test
    fun parseRequiredElement_shouldExtractCorrectLink() {
        val expectedLink = "https://devblogs.microsoft.com/surface-duo/build-and-test-dual-screen-web-apps/"
        val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        xmlPullParser.setInput(
            StringReader("<${RssFeed.LINK}>$expectedLink</${RssFeed.LINK}>")
        )
        xmlPullParser.next()

        val actualItem = RssItem(null, null, null, null, null)

        assertEquals(
            "Tag Name should be ${RssFeed.LINK}",
            RssFeed.LINK,
            xmlPullParser.name
        )

        RssFeed.parseRequiredElement(xmlPullParser, actualItem, RssFeed.LINK)
        assertEquals(
            "Tag of ${RssFeed.LINK} should have $expectedLink value",
            expectedLink,
            actualItem.href
        )
    }

    @Test
    fun parseRequiredElement_shouldExtractCorrectDescription() {
        val expectedDescription = "Hello dual-screen web developers!"
        val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        xmlPullParser.setInput(
            StringReader("<${RssFeed.DESCRIPTION}>$expectedDescription</${RssFeed.DESCRIPTION}>")
        )
        xmlPullParser.next()

        val actualItem = RssItem(null, null, null, null, null)

        assertEquals(
            "Tag Name should be ${RssFeed.DESCRIPTION}",
            RssFeed.DESCRIPTION,
            xmlPullParser.name
        )

        RssFeed.parseRequiredElement(xmlPullParser, actualItem, RssFeed.DESCRIPTION)

        assertEquals(
            "Tag of ${RssFeed.DESCRIPTION} should have $expectedDescription value",
            expectedDescription,
            actualItem.body
        )
    }
}
