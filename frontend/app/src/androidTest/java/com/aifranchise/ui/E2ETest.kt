package com.aifranchise.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aifranchise.MainActivity
import com.aifranchise.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class E2ETest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testLoginFlow() {
        // 1. Enter Email
        onView(withId(R.id.etEmail))
            .perform(typeText("owner@example.com"), closeSoftKeyboard())

        // 2. Enter Password
        onView(withId(R.id.etPassword))
            .perform(typeText("password123"), closeSoftKeyboard())

        // 3. Click Login
        onView(withId(R.id.btnLogin)).perform(click())

        // 4. Verify Dashboard Launched (Check for Dashboard Title)
        // Assume R.id.tvDashboardTitle exists or use text match
        // onView(withText("Dashboard")).check(matches(isDisplayed()))
    }

    @Test
    fun testSalesSubmissionFlow() {
        // Prerequisite: Login (Mocked or Re-run)
        testLoginFlow() 

        // 1. Navigate to Sales Fragment (if not already there)
        // onView(withId(R.id.nav_sales)).perform(click())

        // 2. Enter Amount
        onView(withId(R.id.etAmount))
            .perform(replaceText("5000"), closeSoftKeyboard())

        // 3. Select Payment Mode (Spinner or Radio)
        // simplified for example:
        // onView(withId(R.id.spinnerPaymentMode)).perform(click())
        // onView(withText("Cash")).perform(click())

        // 4. Submit
        onView(withId(R.id.btnSubmitSales)).perform(click())

        // 5. Verify Success Message (Toast or Snackbar or text update)
        // onView(withText("Sales Submitted Successfully")).check(matches(isDisplayed()))
    }
}
