package com.example.storyapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.local.entity.StoryEntity

class HomeViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    val stories: LiveData<PagingData<StoryEntity>> =
        storyRepository.getStories().cachedIn(viewModelScope)

    val totalData: LiveData<Int> = storyRepository.getTotalData()
    fun deleteSession() {
        storyRepository.deleteSession()
    }
}