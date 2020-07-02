/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.sourceeditor

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
    fun configureSpanning() {
//        resetToLeftScreen()
        spanFromLeft()
        assert(isSpanned())
//        unspanToRight()
//        assertFalse(isSpanned())
//        spanFromRight()
//        assert(isSpanned())
//        unspanToLeft()
//        assertFalse(isSpanned())
//        switchToRight()
//        switchToLeft()
    }
    companion object {
        // testing device
        val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        /**
         * X-COORDINATES (pixels)
         */
        // middle of left screen
        const val leftX: Int = 675
        // middle of right screen
        const val rightX: Int = 2109
        // left of hinge area
        const val leftMiddleX: Int = 1340
        // right of hinge area
        const val rightMiddleX: Int = 1434
        /**
         * Y-COORDINATES (pixels)
         */
        // bottom of screen
        const val bottomY: Int = 1780
        // middle of screen
        const val middleY: Int = 900
        /**
         * ANIMATION STEPS
         */
        // panning swipe
        const val spanSteps: Int = 400
        // unspanning swipe
        const val unspanSteps: Int = 140
        // switch from one screen to the other
        const val switchSteps: Int = 600
    }
    private fun spanFromLeft() {
        device.swipe(leftX, bottomY, leftMiddleX, middleY, spanSteps)
    }
    private fun unspanToLeft() {
        device.swipe(leftMiddleX, bottomY, leftX, middleY, unspanSteps)
    }
    private fun spanFromRight() {
        device.swipe(rightX, bottomY, rightMiddleX, middleY, spanSteps)
    }
    private fun unspanToRight() {
        device.swipe(rightMiddleX, bottomY, rightX, middleY, unspanSteps)
    }
    private fun switchToLeft() {
        device.swipe(rightMiddleX, bottomY, leftX, middleY, switchSteps)
    }
    private fun switchToRight() {
        device.swipe(leftMiddleX, bottomY, rightX, middleY, switchSteps)
    }
    private fun isSpanned(): Boolean {
        return ScreenHelper.isDualMode(activityRule.activity)
    }
}
