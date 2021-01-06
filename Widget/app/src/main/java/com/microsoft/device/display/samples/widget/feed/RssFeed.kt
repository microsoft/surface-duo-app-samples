/*
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.widget.feed

import android.content.Context
import android.text.Html
import android.util.Log
import androidx.preference.PreferenceManager
import com.microsoft.device.display.samples.widget.R
import com.microsoft.device.display.samples.widget.network.NetworkFeed
import com.microsoft.device.display.samples.widget.network.RssSimpleApi
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader
import kotlin.math.min

object RssFeed {

    const val DEFAULT_FEED_URL = "https://devblogs.microsoft.com/surface-duo/feed/"
    val TAG = RssFeed::class.java.simpleName
    const val DESCRIPTION_MAX_CHARS = 200
    private const val URL_FEED_PREFERRED_INDEX = 2
    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val LINK = "link"
    const val PUB_DATE = "pubDate"
    const val CREATOR = "dc:creator"
    const val CHANNEL = "channel"
    const val ITEM = "item"
    const val REQUEST_HEADER_VALUE = "application/rss+xml"

    fun refreshItems(context: Context): List<RssItem?>? {
        val baseUrl = getFeedUrlFromPreferences(context)
        val response = NetworkFeed.fetchRssFeed(baseUrl, RssSimpleApi::class.java)
        if (response != null) {
            return parseXml(response)
        }
        return null
    }

    fun parseXml(xmlString: String): List<RssItem?> {
        val rssItems: MutableList<RssItem?> = ArrayList()
        try {
            val xmlFactoryObject = XmlPullParserFactory.newInstance()
            val xmlPullParser = xmlFactoryObject.newPullParser()
            xmlPullParser.setInput(StringReader(xmlString))
            var rssItem: RssItem? = null
            var endOfFeed = false
            var eventType = xmlPullParser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT && !endOfFeed) {
                if (eventType == XmlPullParser.START_TAG) {
                    rssItem = handleStartTagElement(xmlPullParser, rssItem)
                } else if (eventType == XmlPullParser.END_TAG) {
                    val tag = xmlPullParser.name
                    if (shouldInsertItemInRssItems(tag, rssItem)) {
                        rssItems.add(rssItem)
                    } else {
                        endOfFeed = isEndOfFeed(tag)
                    }
                }
                eventType = xmlPullParser.next()
            }
        } catch (e: XmlPullParserException) {
            Log.d(TAG, e.toString())
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }
        return rssItems
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun handleStartTagElement(
        xmlPullParser: XmlPullParser,
        item: RssItem?
    ): RssItem? {
        var auxItem = item
        val tag = xmlPullParser.name
        if (tag.equals(ITEM, ignoreCase = true)) {
            auxItem = RssItem(null, null, null, null, null)
        } else auxItem?.let { parseRequiredElement(xmlPullParser, it, tag) }
        return auxItem
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun parseRequiredElement(
        xmlPullParser: XmlPullParser,
        item: RssItem,
        tag: String
    ) {
        when (tag) {
            TITLE -> item.title = parseTitle(xmlPullParser.nextText())
            DESCRIPTION -> item.body = parseDescription(xmlPullParser.nextText())
            LINK -> item.href = xmlPullParser.nextText()
            PUB_DATE -> item.date = parseDate(xmlPullParser.nextText())
            CREATOR -> item.creator = xmlPullParser.nextText()
        }
    }

    private fun shouldInsertItemInRssItems(tag: String, rssItem: RssItem?): Boolean {
        return tag.equals(ITEM, ignoreCase = true) && rssItem != null
    }

    private fun isEndOfFeed(tag: String): Boolean {
        return tag.equals(CHANNEL, ignoreCase = true)
    }

    private fun parseTitle(title: String): String {
        return stripHtml(title)
    }

    private fun parseDescription(description: String): String {
        val descriptionWithEnding = description.substring(
            0,
            min(description.length, DESCRIPTION_MAX_CHARS)
        ) + "..."
        return stripHtml(descriptionWithEnding)
    }

    private fun parseDate(date: String): String {
        return date.substring(0, date.length - 6)
    }

    private fun stripHtml(html: String): String {
        return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
            .toString()
            .replace(
                "[&*?;]".toRegex(),
                ""
            )
    }

    private fun getFeedUrlFromPreferences(context: Context): String {
        // Taking current custom feed
        val customPreferredFeed = getPreference(
            context.resources.getString(R.string.widget_settings_predefined_key),
            context
        )

        // Taking current selected value from preferred feeds
        val customPreferredFeedValuesArray = context.resources
            .getStringArray(R.array.appwidget_feeds_value)

        // If custom enabled, take custom feed
        // Else fallback to preferred feed
        val returnFeed: String?
        returnFeed = if (customPreferredFeed == customPreferredFeedValuesArray[URL_FEED_PREFERRED_INDEX]) {
            getPreference(
                context.resources.getString(R.string.widget_settings_custom_key),
                context
            )
        } else {
            customPreferredFeed
        }

        // Prevents URL exception
        return if (returnFeed!!.endsWith("/")) {
            returnFeed
        } else {
            "$returnFeed/"
        }
    }

    private fun getPreference(key: String, context: Context): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, DEFAULT_FEED_URL)
    }
}