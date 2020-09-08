package com.microsoft.device.display.samples.widget

import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.microsoft.device.display.samples.widget.feed.RssFeed
import com.microsoft.device.display.samples.widget.feed.RssFeed.DEFAULT_FEED_URL
import com.microsoft.device.display.samples.widget.settings.SettingsActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@MediumTest
class SettingsUrlTest {

    @get:Rule
    val activityRule = ActivityTestRule<SettingsActivity>(SettingsActivity::class.java)

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearPreferences() {
        PreferenceManager.getDefaultSharedPreferences(appContext).edit().clear().commit()
    }

    @Test
    fun shouldBeDefaultFeedUrl_whenGettingRequestUrlFromPreferences() {
        assertEquals(
            "Base Url from Retrofit should be Default Feed Url",
            DEFAULT_FEED_URL,
            RssFeed.configureNetworkCall(appContext).baseUrl().toString()
        )
        assertEquals(
            "Preferences Feed Url should be Default Feed Url",
            DEFAULT_FEED_URL,
            RssFeed.getFeedUrlFromPreferences(appContext)
        )
    }

    @Test
    fun shouldSavePreference_whenAnotherFeedUrlIsChosen() {
        onView(withText(R.string.widget_settings_predefined_title)).check(matches(isDisplayed()))
        onView(withText(R.string.widget_settings_custom_title)).check(matches(isDisplayed()))

        onView(withText(R.string.widget_settings_predefined_title)).perform(click())

        val secondKey = appContext.resources.getStringArray(R.array.appwidget_feeds)[1]
        val secondValue = appContext.resources.getStringArray(R.array.appwidget_feeds_value)[1]
        onView(withText(secondKey)).perform(click())

        assertEquals(
            "Base Url from Retrofit should be second value url",
            secondValue,
            RssFeed.configureNetworkCall(appContext).baseUrl().toString()
        )
        assertEquals(
            "Preferences Feed Url should be second value url",
            secondValue,
            RssFeed.getFeedUrlFromPreferences(appContext)
        )
    }
}
