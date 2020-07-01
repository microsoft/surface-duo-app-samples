package com.microsoft.device.display.samples.photoeditor

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.layout.ScreenHelper
import com.microsoft.device.dualscreen.layout.SurfaceDuoLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class PhotoEditorUITest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.microsoft.device.display.samples.photoeditor", appContext.packageName)
    }

    /**
     * Tests visibility of controls when app spanned vs. unspanned
     */
    @Test
    fun testControlVisibility() {
        // App opens in single-screen mode, so dropdown and saturation slider should be visible while brightness and warmth sliders are hidden
        onView(withId(R.id.controls)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.saturation)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.brightness)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        onView(withId(R.id.warmth)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))

        spanFromLeft()
        assert(isSpanned())

        // Switched to dual-screen mode, so dropdown should not exist and all sliders should be visible
        onView(withId(R.id.controls)).check(doesNotExist())
        onView(withId(R.id.saturation)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.brightness)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.warmth)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        unspanToLeft()
        assertFalse(isSpanned())
    }


    /**
     * HELPER FUNCTIONS FOR DUAL-SCREEN BEHAVIOR
     *
     * Use the functions below in your tests when testing dual-screen behaviors and transitions.
     * Uncomment and run the "configureSpanning" test below to check that the methods produce
     * the expected behavior on your device.
     *
     * If the test fails, modify the swipe parameters as needed- usually either an increase in the
     * "steps" parameter or a slight shift in the "endX" parameter.
     *
     * See the link below for Surface Duo pixel information:
     * https://devblogs.microsoft.com/surface-duo/resource-configuration-for-microsoft-surface-duo/
     */
//    @Test
//    fun configureSpanning() {
//        spanFromLeft()
//        assert(isSpanned())
//
//        unspanToRight()
//        assertFalse(isSpanned())
//
//        spanFromRight()
//        assert(isSpanned())
//
//        unspanToLeft()
//        assertFalse(isSpanned())
//    }

    companion object {
        // your testing device
        val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // x-coordinate for middle of left screen
        const val leftX: Int = 675

        // x-coordinate for middle of right screen
        const val rightX: Int = 2105

        // y-coordinate for bottom of screen
        const val bottomY: Int = 1780

        // x-coordinate for left of hinge area
        const val leftMiddleX: Int = 1300

        // x-coordinate for right of hinge area
        const val rightMiddleX: Int = 1500

        // y-coordinate for middle of screen
        const val middleY: Int = 900

        // number of steps to take to complete a spanning swipe
        const val spanSteps: Int = 400

        // number of steps to take to complete an unspanning swipe
        const val unspanSteps: Int = 140
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

//    private fun isSpanned(): Matcher<View?> {
//        return ScreenSizeMatcher()
//    }

    private fun isSpanned(): Boolean {
        return ScreenHelper.isDualMode(activityRule.activity)
    }
}

//class ScreenSizeMatcher() :
//    TypeSafeMatcher<View?>(View::class.java) {
//
//    private val middle = 1392
//
//    override fun matchesSafely(target: View?): Boolean {
//        if (target !is FrameLayout) {
//            return false
//        }
//        // Compare layout size to single screen size
//
//        // notes: target.parent = SurfaceDuoLayout, not sure which properties to go to from there
//        return target.width > middle
//    }
//
//    override fun describeTo(description: Description) {
//        description.appendText("isSpanned")
//    }
//}
