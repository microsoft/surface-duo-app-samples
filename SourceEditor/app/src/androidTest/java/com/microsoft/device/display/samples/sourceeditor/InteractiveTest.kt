/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.sourceeditor

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.layout.ScreenHelper
import org.hamcrest.core.StringContains.containsString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class InteractiveTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun useAppContext() { // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.microsoft.device.display.samples.sourceeditor", appContext.packageName)
    }

    @Test
    fun textTransferTest() {
        onView(withId(R.id.btn_switch_to_preview)).perform(click())
        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "h1"))
            .check(webMatches(getText(), containsString("Testing in a browser")))
    }

    @Test
    fun textPreservedOnSpanTest() {
        onView(withId(R.id.textinput_code)).perform(replaceText("<h1>" + testString +  "</h1>"))
        spanFromLeft()
        assert(isSpanned())
        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "h1"))
            .check(webMatches(getText(), containsString(testString)))
    }

    @Test
    fun testSpanning() {
        spanFromLeft()
        assert(isSpanned())
        unspanToRight()
        assertFalse(isSpanned())
        spanFromRight()
        assert(isSpanned())
        unspanToLeft()
        assertFalse(isSpanned())
        switchToRight()
        switchToLeft()
    }

    companion object {
        // testing device
        val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        const val leftX: Int = 675          // middle of left screen
        const val rightX: Int = 2109        // middle of right screen
        const val leftMiddleX: Int = 1340   // left of hinge area
        const val rightMiddleX: Int = 1434  // right of hinge area
        const val bottomY: Int = 1780       // bottom of screen
        const val middleY: Int = 900        // middle of screen
        const val spanSteps: Int = 400      // spanning/unspanning swipe
        const val switchSteps: Int = 600    // switch from one screen to the other

        const val testString = "Testing in a different browser"
    }
    private fun spanFromLeft() {
        device.swipe(leftX, bottomY, leftMiddleX, middleY, spanSteps)
    }
    private fun unspanToLeft() {
        device.swipe(rightX, bottomY, leftX, middleY, spanSteps)
    }
    private fun spanFromRight() {
        device.swipe(rightX, bottomY, rightMiddleX, middleY, spanSteps)
    }
    private fun unspanToRight() {
        device.swipe(leftX, bottomY, rightX, middleY, spanSteps)
    }
    private fun switchToLeft() {
        device.swipe(rightX, bottomY, leftX, middleY, switchSteps)
    }
    private fun switchToRight() {
        device.swipe(leftX, bottomY, rightX, middleY, switchSteps)
    }
    private fun isSpanned(): Boolean {
        return ScreenHelper.isDualMode(activityRule.activity)
    }
}
