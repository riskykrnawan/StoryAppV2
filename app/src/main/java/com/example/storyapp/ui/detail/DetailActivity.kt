package com.example.storyapp.ui.detail

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.Result
import com.example.storyapp.data.remote.response.Story
import com.example.storyapp.databinding.ActivityDetailBinding
import com.example.storyapp.helper.SessionPreferences
import com.example.storyapp.helper.Utils.withDateFormat
import com.example.storyapp.helper.ViewModelFactory
import com.example.storyapp.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("sessions")

class DetailActivity : AppCompatActivity() {

    private var _activityDetailBinding: ActivityDetailBinding? = null
    private val binding get() = _activityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityDetailBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarMain)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val pref = SessionPreferences.getInstance(dataStore)
        val factory = ViewModelFactory.getInstance(this@DetailActivity.application, pref)
        val detailViewModel: DetailViewModel by viewModels { factory }

        checkToken(pref)

        val storyId = intent.getStringExtra(EXTRA_ID)

        if (storyId != null) {
            detailViewModel.getStoryById(storyId).observe(this) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> {
                            binding?.progressBar?.visibility = View.VISIBLE
                        }

                        is Result.Success -> {
                            binding?.progressBar?.visibility = View.GONE
                            val story = result.data
                            setStoryData(story)
                            playAnimation()
                            supportActionBar?.apply {
                                setDisplayHomeAsUpEnabled(true)
                                setDisplayShowTitleEnabled(true)
                                title = story.name
                            }
                        }

                        is Result.Error -> {
                            binding?.progressBar?.visibility = View.GONE
                            Toast.makeText(
                                this,
                                getString(R.string.error) + result.error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkToken(pref: SessionPreferences) {
        val token = runBlocking { pref.getToken().first() }
        if (token == "") {
            startActivity(Intent(this@DetailActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun setStoryData(story: Story) {
        binding?.ivDetailPhoto?.let {
            Glide.with(applicationContext).load(story.photoUrl).into(it)
        }
        binding?.tvDetailName?.text = story.name
        binding?.tvDetailCreatedAt?.text = story.createdAt.withDateFormat()
        binding?.tvDetailDescription?.text = story.description
    }

    private fun playAnimation() {
        val ivDetailPhoto =
            ObjectAnimator.ofFloat(binding?.ivDetailPhoto, View.ALPHA, 1f).setDuration(300)
        val tvDetailName =
            ObjectAnimator.ofFloat(binding?.tvDetailName, View.ALPHA, 1f).setDuration(300)
        val tvDetailCreatedAt =
            ObjectAnimator.ofFloat(binding?.tvDetailCreatedAt, View.ALPHA, 1f).setDuration(300)
        val tvDetailDescription =
            ObjectAnimator.ofFloat(binding?.tvDetailDescription, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(ivDetailPhoto, tvDetailName, tvDetailCreatedAt, tvDetailDescription)
            start()
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}