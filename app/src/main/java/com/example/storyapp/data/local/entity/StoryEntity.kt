package com.example.storyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "stories")
data class StoryEntity(

    @field:SerializedName("photoUrl") val photoUrl: String,

    @field:SerializedName("createdAt") val createdAt: String,

    @field:SerializedName("name") val name: String,

    @field:SerializedName("description") val description: String,

    @field:SerializedName("lon") val lon: Float? = null,

    @field:PrimaryKey @field:SerializedName("id") val id: String,

    @field:SerializedName("lat") val lat: Float? = null
)
