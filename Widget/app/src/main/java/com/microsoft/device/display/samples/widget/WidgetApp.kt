/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.Toast
import com.microsoft.device.display.samples.widget.settings.SettingsActivity

/**
 * Implementation of App Widget functionality.
 */
class WidgetApp : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE = "com.microsoft.device.display.samples.widget.action.ACTION_UPDATE"
        const val ACTION_SETTINGS = "com.microsoft.device.display.samples.widget.action.ACTION_SETTINGS"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {

            // Adding Remote Views adapter for every widget instance
            val views = RemoteViews(
                context.packageName,
                R.layout.collection_widget
            )
            val intent = Intent(context, WidgetService::class.java)
            views.setRemoteAdapter(R.id.widgetListView, intent)

            // Adding pendingIntent for widget list View
            val clickIntent = Intent(context, WidgetApp::class.java)
            clickIntent.action = WidgetFactory.ACTION_INTENT_VIEW_TAG
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val clickPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Adding update button logic for every widget instance
            views.setOnClickPendingIntent(
                R.id.widgetUpdateButton,
                getPendingSelfIntent(context, ACTION_UPDATE)
            )

            // Adding settings button logic for every widget instance
            views.setOnClickPendingIntent(
                R.id.widgetSettingsButton,
                getPendingSelfIntent(context, ACTION_SETTINGS)
            )

            // Update all widgets instances
            views.setPendingIntentTemplate(
                R.id.widgetListView,
                clickPendingIntent
            )
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Handling intent for list item click
        if (WidgetFactory.ACTION_INTENT_VIEW_TAG.equals(intent.action)) {
            val url =
                intent.getStringExtra(WidgetFactory.ACTION_INTENT_VIEW_HREF_TAG)
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            webIntent.addFlags(
                Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                    Intent.FLAG_ACTIVITY_NEW_TASK
            )
            context.startActivity(webIntent)
        }

        // Handing intent for list items update
        if (ACTION_UPDATE == intent.action) {
            onUpdate(context)
        }

        // Handling intent for settings screen launch
        if (ACTION_SETTINGS == intent.action) {
            val intentSettings = Intent(context, SettingsActivity::class.java)
            intentSettings.addFlags(
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                    Intent.FLAG_ACTIVITY_NEW_TASK
            )
            context.startActivity(intentSettings)
        }

        super.onReceive(context, intent)
    }

    // Private method used to call the generic update method from above
    private fun onUpdate(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val cn = ComponentName(context, WidgetApp::class.java)
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.widgetListView)
        Toast.makeText(context, "Your widget will be updated soon! ", Toast.LENGTH_SHORT)
            .show()
    }

    // An explicit intent directed at the current class (the "self").
    private fun getPendingSelfIntent(context: Context, action: String): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
