package com.example.storyapp.helper

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.storyapp.data.remote.response.LoginResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    fun getToken(): Flow<String> = dataStore.data.map { preferences ->
        preferences[TOKEN] ?: ""
    }

    suspend fun saveSession(user: LoginResponse) {
        dataStore.edit { preferences ->
            preferences[TOKEN] = user.loginResult?.token.toString()
        }
    }

    suspend fun deleteSession() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val TOKEN = stringPreferencesKey("session_token")

        @Volatile
        private var INSTANCE: SessionPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): SessionPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}