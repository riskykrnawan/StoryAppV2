package com.example.storyapp.data.local

data class UserModelRegister(
    val name: String, val email: String, val password: String
)

data class UserModelLogin(
    val email: String, val password: String
)