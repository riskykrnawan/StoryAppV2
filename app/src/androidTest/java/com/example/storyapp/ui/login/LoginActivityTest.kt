package com.example.storyapp.ui.login

import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.storyapp.R
import com.example.storyapp.helper.EspressoIdlingResource
import com.example.storyapp.ui.home.HomeActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {
    private val email = "bungul0@gmail.com"
    private val password = "00000000"

    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun loginWithCorrectCredential() {
        Intents.init()
        // Login
        onView(withId(R.id.btn_login)).check(matches(isNotEnabled()))
        onView(withId(R.id.ed_login_email)).perform(typeText(email))
        onView(withId(R.id.ed_login_password)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).check(matches(isEnabled()))
        onView(withId(R.id.btn_login)).perform(click())
        onView(withId(R.id.progressBar)).check(matches(isCompletelyDisplayed()))

        // Logout
        sleep(10000L)
        intended(hasComponent(HomeActivity::class.java.name))
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext())
        onView(withText(R.string.logout)).perform(click())

        sleep(5000L)
        intended(hasComponent(LoginActivity::class.java.name))
        onView(withText(R.string.register_now)).check(matches(isDisplayed()))
    }
}
