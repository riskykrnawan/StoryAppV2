package com.example.storyapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.storyapp.data.local.UserModelLogin
import com.example.storyapp.data.local.UserModelRegister
import com.example.storyapp.data.local.entity.StoryEntity
import com.example.storyapp.data.local.room.StoryDatabase
import com.example.storyapp.data.remote.response.DetailStoryResponse
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.remote.response.LoginResponse
import com.example.storyapp.data.remote.response.StoriesResponse
import com.example.storyapp.data.remote.response.Story
import com.example.storyapp.data.remote.response.SuccessResponse
import com.example.storyapp.data.remote.retrofit.ApiService
import com.example.storyapp.helper.SessionPreferences
import com.example.storyapp.helper.Utils
import com.example.storyapp.helper.wrapEspressoIdlingResource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val pref: SessionPreferences,
) {
    private val stories = MediatorLiveData<Result<List<ListStoryItem>>>()
    private val story = MediatorLiveData<Result<Story>>()
    private val user = MediatorLiveData<Result<LoginResponse>>()
    private val add = MediatorLiveData<Result<SuccessResponse>>()

    fun postStory(
        file: File, description: String, lat: Float?, lon: Float?
    ): LiveData<Result<SuccessResponse>> {
        add.value = Result.Loading
        val reducedFile = Utils.reduceFileImage(file)
        val requestImageFile = reducedFile.asRequestBody("image/jpeg".toMediaType())

        val requestDescription = description.toRequestBody("text/plain".toMediaType())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo", reducedFile.name, requestImageFile
        )

        val token = runBlocking { pref.getToken().first() }
        val client =
            apiService.postStory("Bearer $token", imageMultipart, requestDescription, lat, lon)

        client.enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>, response: Response<SuccessResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        add.value = Result.Success(responseBody)
                    }
                } else {
                    add.value = Result.Error(response.message())
                }
            }

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                add.value = t.message?.let { Result.Error(it) }
            }
        })
        return add
    }

    fun register(name: String, email: String, password: String): LiveData<Result<SuccessResponse>> {
        add.value = Result.Loading
        val client = apiService.register(UserModelRegister(name, email, password))

        client.enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>, response: Response<SuccessResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        add.value = Result.Success(responseBody)
                    }
                } else {
                    add.value = Result.Error(response.message())
                }
            }

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                add.value = t.message?.let { Result.Error(it) }
            }
        })
        return add
    }

    fun login(email: String, password: String): LiveData<Result<LoginResponse>> {
        user.value = Result.Loading
        val client = apiService.login(UserModelLogin(email, password))
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>, response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        saveSession(responseBody)
                        user.value = Result.Success(responseBody)
                    }
                } else {
                    user.value = Result.Error(response.message())
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                user.value = t.message?.let { Result.Error(it) }
            }
        })
        return user
    }

    fun getStories(): LiveData<PagingData<StoryEntity>> {
        wrapEspressoIdlingResource {
            val token = runBlocking { pref.getToken().first() }
            @OptIn(ExperimentalPagingApi::class)
            return Pager(
                config = PagingConfig(
                    pageSize = 5
                ),
                remoteMediator = StoryRemoteMediator(storyDatabase, apiService, "Bearer $token"),
                pagingSourceFactory = {
                    storyDatabase.storyDao().getStories()
                }
            ).liveData
        }
    }

    fun getTotalData(): LiveData<Int> {
        return storyDatabase.storyDao().getTotalData()
    }

    fun getStoriesWithLocation(): LiveData<Result<List<ListStoryItem>>> {
        stories.value = Result.Loading
        val token = runBlocking { pref.getToken().first() }
        val client = apiService.getStoriesWithLocation("Bearer $token")
        client.enqueue(object : Callback<StoriesResponse> {
            override fun onResponse(
                call: Call<StoriesResponse>, response: Response<StoriesResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        stories.value = Result.Success(responseBody.listStory)
                    }
                }
            }

            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                stories.value = Result.Error(t.message.toString())
            }
        })

        return stories
    }

    fun getStoryById(id: String): LiveData<Result<Story>> {
        story.value = Result.Loading
        val token = runBlocking { pref.getToken().first() }
        val client = apiService.getStoryById("Bearer $token", id)

        client.enqueue(object : Callback<DetailStoryResponse> {
            override fun onResponse(
                call: Call<DetailStoryResponse>, response: Response<DetailStoryResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        story.value = Result.Success(responseBody.story)
                    }
                }
            }

            override fun onFailure(call: Call<DetailStoryResponse>, t: Throwable) {
                story.value = Result.Error(t.message.toString())
            }
        })
        return story
    }

    fun saveSession(response: LoginResponse) {
        runBlocking {
            pref.saveSession(response)
        }
    }

    fun deleteSession() {
        runBlocking { pref.deleteSession() }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            storyDatabase: StoryDatabase, apiService: ApiService, pref: SessionPreferences
        ): StoryRepository = instance ?: synchronized(this) {
            instance ?: StoryRepository(storyDatabase, apiService, pref)
        }.also { instance = it }
    }
}