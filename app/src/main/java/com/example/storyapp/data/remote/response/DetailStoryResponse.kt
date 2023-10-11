package com.example.storyapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class DetailStoryResponse(

    @field:SerializedName("error") val error: Boolean? = null,

    @field:SerializedName("message") val message: String? = null,

    @field:SerializedName("story") val story: Story
)

data class Story(

    @field:SerializedName("photoUrl") val photoUrl: String,

    @field:SerializedName("createdAt") val createdAt: String,

    @field:SerializedName("name") val name: String,

    @field:SerializedName("description") val description: String,

    @field:SerializedName("lon") val lon: Any? = null,

    @field:SerializedName("id") val id: String,

    @field:SerializedName("lat") val lat: Any? = null
)
