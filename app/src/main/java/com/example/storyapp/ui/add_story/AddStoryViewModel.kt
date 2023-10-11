package com.example.storyapp.ui.add_story

import androidx.lifecycle.ViewModel
import com.example.storyapp.data.StoryRepository
import java.io.File

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun postStory(file: File, description: String, lat: Float?, lon: Float?) = storyRepository.postStory(file, description, lat, lon)
}