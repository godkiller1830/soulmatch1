package com.example.matrimony

data class UserProfile(
    val uid: String,
    val name: String,
    val age: Int,
    val bio: String,
    val interests: List<String>
)
