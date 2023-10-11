package com.example.storyapp.data.local.room

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.storyapp.data.local.entity.StoryEntity

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(story: List<StoryEntity>)

    @Query("SELECT * FROM stories")
    fun getStories(): PagingSource<Int, StoryEntity>

    @Query("SELECT COUNT(*) as total_data FROM stories")
    fun getTotalData(): LiveData<Int>

    @Query("DELETE FROM stories")
    suspend fun deleteAll()
}