/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */
package com.microsoft.device.display.samples.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.microsoft.device.display.samples.widget.feed.RssFeed
import com.microsoft.device.display.samples.widget.feed.RssItem

class WidgetFactory(private val context: Context, private val intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {

    companion object {
        const val ACTION_INTENT_VIEW_TAG = "action_intent_view_tag"
        const val ACTION_INTENT_VIEW_HREF_TAG = "action_intent_view_href_tag"
    }

    private val rssItems: MutableList<RssItem?> = ArrayList()

    override fun onCreate() { }

    override fun onDataSetChanged() {
        RssFeed.refreshItems(context)?.let { newData ->
            rssItems.clear()
            rssItems.addAll(newData)
        }
    }

    override fun onDestroy() {
        rssItems.clear()
    }

    override fun getCount(): Int {
        return rssItems.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val rv = RemoteViews(
            context.packageName,
            R.layout.collection_widget_list_item
        )

        rssItems[position]?.let { item ->
            // Widget title
            rv.setTextViewText(
                R.id.widget_item_title,
                item.title
            )

            // Widget Creator
            rv.setTextViewText(
                R.id.widget_item_creator,
                item.creator
            )

            // Widget date
            rv.setTextViewText(
                R.id.widget_item_date,
                item.date
            )

            // Widget text body
            rv.setTextViewText(
                R.id.widget_item_content,
                item.body
            )

            // Intent for each list element where we add http link for each post
            addIntentForWidgetItem(rv, item)
        }

        return rv
    }

    private fun addIntentForWidgetItem(
        remoteViews: RemoteViews,
        rssItem: RssItem
    ) {
        val extras = Bundle()
        extras.putString(ACTION_INTENT_VIEW_HREF_TAG, rssItem.href)
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)
        remoteViews.setOnClickFillInIntent(R.id.widgetItemContainer, fillInIntent)
    }

    override fun getLoadingView(): RemoteViews? {
        return RemoteViews(
            context.packageName,
            R.layout.collection_widget_list_item_loading
        )
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}