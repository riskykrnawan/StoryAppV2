package com.example.storyapp.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivityHomeBinding
import com.example.storyapp.helper.SessionPreferences
import com.example.storyapp.helper.ViewModelFactory
import com.example.storyapp.ui.add_story.AddStoryActivity
import com.example.storyapp.ui.login.LoginActivity
import com.example.storyapp.ui.maps.MapsActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("sessions")

class HomeActivity : AppCompatActivity() {

    private var _activityHomeBinding: ActivityHomeBinding? = null
    private val binding get() = _activityHomeBinding

    private val homeViewModel: HomeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityHomeBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarMain)
        supportActionBar?.title = resources.getString(R.string.app_name)

        // viewModel
        val pref = SessionPreferences.getInstance(dataStore)
        val factory = ViewModelFactory.getInstance(this@HomeActivity.application, pref)
        val homeViewModel: HomeViewModel by viewModels { factory }

        checkToken(pref)

        binding?.fabActionAdd?.setOnClickListener {
            val intent = Intent(this@HomeActivity, AddStoryActivity::class.java)
            startActivity(intent)
        }

        getData(homeViewModel)
        binding?.rvStories?.layoutManager = LinearLayoutManager(this)
    }

    private fun getData(homeViewModel: HomeViewModel) {
        val adapter = StoryAdapter()
        binding?.rvStories?.adapter = adapter.withLoadStateFooter(footer = LoadingStateAdapter {
            adapter.retry()
        })

        homeViewModel.totalData.observe(this) {
            if (it == 0) {
                binding?.tvEmptyData?.visibility = View.VISIBLE
            } else {
                binding?.tvEmptyData?.visibility = View.INVISIBLE
            }
        }

        homeViewModel.stories.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_maps -> {
                startActivity(Intent(this@HomeActivity, MapsActivity::class.java))
                true
            }

            R.id.action_setting -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }

            R.id.action_logout -> {
                homeViewModel.deleteSession()
                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkToken(pref: SessionPreferences) {
        val token = runBlocking { pref.getToken().first() }
        if (token == "") {
            startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _activityHomeBinding = null
    }
}