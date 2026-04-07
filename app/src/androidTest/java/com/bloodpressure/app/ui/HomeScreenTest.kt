package com.bloodpressure.app.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun homeScreen_launchesSuccessfully() {
        onView(withText("血压日记"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_displaysTitle() {
        onView(withText("血压日记"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_refreshButtonIsVisible() {
        onView(withText("刷新"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_historyButtonIsVisible() {
        onView(withText("历史记录"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_settingsButtonIsVisible() {
        onView(withText("设置"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_trendCardIsVisible() {
        onView(withText("最近7天趋势"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_todayRecordsCardIsVisible() {
        onView(withText("今日记录"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_quickTipsCardIsVisible() {
        onView(withText("测量建议"))
            .check(matches(isDisplayed()))
    }
}
