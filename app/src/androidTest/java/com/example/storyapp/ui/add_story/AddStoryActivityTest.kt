package com.example.storyapp.ui.add_story

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.storyapp.R
import com.example.storyapp.helper.EspressoIdlingResource
import com.example.storyapp.ui.detail.DetailActivity
import com.example.storyapp.ui.home.HomeActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("sessions")

@RunWith(AndroidJUnit4::class)
@LargeTest
class AddStoryActivityTest {
    private val description = "UI Test"

    private val email = "bungul0@gmail.com"
    private val password = "00000000"

    @get:Rule
    val activity = ActivityScenarioRule(AddStoryActivity::class.java)

    @get:Rule
    val locationRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val cameraRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        login()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }

    private fun login() {
        // Login terlebih dahulu untuk mendapat token
        Intents.init()
        onView(withId(R.id.ed_login_email)).perform(typeText(email))
        onView(withId(R.id.ed_login_password)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click())

        // HomeActivity untuk mengklik fab button
        sleep(10000L)
        intended(hasComponent(HomeActivity::class.java.name))
        onView(withId(R.id.fab_action_add)).perform(click())
        Intents.release()
    }

    @Test
    fun addNewStory() {
        Intents.init()
        // Upload story
        onView(withText(R.string.upload_photo_story)).check(matches(isDisplayed()))
        onView(withId(R.id.button_add)).check(matches(isNotEnabled()))
        onView(withId(R.id.switch_location)).check(matches(isNotChecked())).perform(click())
        onView(withId(R.id.switch_location)).check(matches(isChecked()))
        onView(withId(R.id.button_camera)).perform(click())

        // Camera
        intended(hasComponent(CameraActivity::class.java.name))
        onView(withId(R.id.captureImage)).perform(click())

        sleep(5000L)
        onView(withId(R.id.ed_add_description)).perform(typeText(description), closeSoftKeyboard())
        onView(withId(R.id.button_add)).perform(click())
        onView(withId(R.id.progressBar)).check(matches(isCompletelyDisplayed()))

        // Kembali ke home
        sleep(5000L)
        intended(hasComponent(HomeActivity::class.java.name))
        onView(withId(R.id.rv_stories)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        sleep(2000L)
        intended(hasComponent(DetailActivity::class.java.name))
        onView(withText(description)).check(matches(isDisplayed()))
    }
}
