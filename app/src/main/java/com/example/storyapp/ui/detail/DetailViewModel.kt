package com.example.storyapp.ui.detail

import androidx.lifecycle.ViewModel
import com.example.storyapp.data.StoryRepository

class DetailViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun getStoryById(id: String) = storyRepository.getStoryById(id)
}