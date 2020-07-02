package com.microsoft.device.display.samples.photoeditor

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.microsoft.device.dualscreen.layout.ScreenHelper
import org.junit.Assert.assertEquals
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
     *
     * @precondition no other applications are open
     */
    @Test
    fun testControlVisibility() {
        // Lock orientation for entire test so spanning/unspanning work as expected
        device.setOrientationNatural()

        // App opens in single-screen mode, so dropdown and saturation slider should be visible while brightness and warmth sliders are hidden
        onView(withId(R.id.controls)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.saturation)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.brightness)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        onView(withId(R.id.warmth)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))

        spanFromLeft()

        // Switched to dual-screen mode, so dropdown should not exist and all sliders should be visible
        onView(withId(R.id.controls)).check(doesNotExist())
        onView(withId(R.id.saturation)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.brightness)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.warmth)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        unspanToLeft()
        device.unfreezeRotation()
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
//        resetToLeftScreen()
//
//        spanFromLeft()
//        require(isSpanned())
//
//        unspanToRight()
//        require(!isSpanned())
//
//        spanFromRight()
//        require(isSpanned())
//
//        unspanToLeft()
//        require(!isSpanned())
//
//        switchToRight()
//        switchToLeft()
//    }

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

        // hinge area
        const val middleX: Int = 1350

        // left of hinge area
        const val leftMiddleX: Int = 1200

        // right of hinge area
        const val rightMiddleX: Int = 1500

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
        const val switchSteps: Int = 50
    }

    private fun spanFromLeft() {
        device.swipe(leftX, bottomY, middleX, middleY, spanSteps)
        device.wait(Until.gone(By.clazz("SeekBar")), 3000)
    }

    private fun unspanToLeft() {
        device.swipe(rightMiddleX, bottomY, leftX, middleY, unspanSteps)
    }

    private fun spanFromRight() {
        device.swipe(rightX, bottomY, middleX, middleY, spanSteps)
    }

    private fun unspanToRight() {
        device.swipe(leftMiddleX, bottomY, rightX, middleY, unspanSteps)
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
