package com.example.storyapp

import com.example.storyapp.data.local.entity.StoryEntity

object DataDummy {
fun generateDummyStoryResponse(): ArrayList<StoryEntity> {
    val listStory = ArrayList<StoryEntity>()
    for (i in 0..10) {
        listStory.add(
            StoryEntity(
                "https://avatars.githubusercontent.com/u/96241592?s=400&u=a4e0f6c7c1d11f04019cf39529f2432f60c4e979&v=4",
                "2023-05-07T06:51:59.151Z",
                "Name $i",
                "Descriptions $i",
                i.toFloat(),
                "ID $i",
                i.toFloat(),
            )
        )
    }
    return listStory
    }
}