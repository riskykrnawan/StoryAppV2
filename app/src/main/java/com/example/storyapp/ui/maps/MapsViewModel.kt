package com.example.storyapp.ui.maps

import androidx.lifecycle.ViewModel
import com.example.storyapp.data.StoryRepository

class MapsViewModel(storyRepository: StoryRepository) : ViewModel() {
    val stories = storyRepository.getStoriesWithLocation()
}