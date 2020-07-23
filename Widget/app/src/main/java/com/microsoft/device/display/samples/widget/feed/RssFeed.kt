package com.microsoft.device.display.samples.widget.feed

import android.content.Context
import android.text.Html
import android.util.Log
import androidx.preference.PreferenceManager
import com.microsoft.device.display.samples.widget.R
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.io.StringReader

/*
*
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License.
*
*/

object RssFeed {

    const val DEFAULT_FEED_URL = "https://devblogs.microsoft.com/surface-duo/feed/"
    val TAG = RssFeed::class.java.simpleName
    const val DESCRIPTION_MAX_CHARS = 200
    const val URL_FEED_PREFERED_INDEX = 2
    private const val TITLE = "title"
    private const val DESCRIPTION = "description"
    private const val LINK = "link"
    private const val PUB_DATE = "pubDate"
    private const val CREATOR = "dc:creator"
    private const val CHANNEL = "channel"
    private const val ITEM = "item"
    private val RSS_ITEMS: MutableList<RssItem?> = ArrayList()

    fun clearRssItems() {
        RSS_ITEMS.clear()
    }

    val rssItemsSize: Int
        get() = RSS_ITEMS.size

    fun getRssItemsItemAt(position: Int): RssItem? {
        return RSS_ITEMS[position]
    }

    fun fetchRssFeed(context: Context) {
        val retrofit = configureNetworkCall(context)
        val rssSimpleApi = retrofit.create(RssSimpleApi::class.java)

        // Making surface duo blog data request syncrhonous since this call is done
        // from onDataSetChanged in the widget
        val request: Call<String> = rssSimpleApi.rssFeed
        try {
            val response = request.execute()
            if (response.isSuccessful) {
                RSS_ITEMS.clear()
                RSS_ITEMS.addAll(parseXml(response.body()!!))
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun configureNetworkCall(context: Context): Retrofit {
        val httpClientBuilder = OkHttpClient.Builder()

        // HttpClient interceptor to add specific rss feeds headers
        // Some of the rss sources require these or else will fetch html instead of rss
        httpClientBuilder.addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.header("Content-Type", "application/rss+xml")
                requestBuilder.header("Accept", "application/rss+xml")
                return chain.proceed(requestBuilder.build())
            }
        })
        val okHttpClient = httpClientBuilder.build()

        // Retrofit constructor for surface duo blog data request
        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(getFeedUrlFromPreferences(context))
            .client(okHttpClient).build()
    }

    private fun parseXml(xmlString: String): List<RssItem?> {
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
            Math.min(description.length, DESCRIPTION_MAX_CHARS)
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
                "&.*?;".toRegex(),
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
        returnFeed = if (customPreferredFeed == customPreferredFeedValuesArray[URL_FEED_PREFERED_INDEX]) {
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