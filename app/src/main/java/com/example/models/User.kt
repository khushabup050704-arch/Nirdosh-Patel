package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "user_default",
    val name: String = "Creator",
    val email: String = "creator@nirdoshvideo.ai",
    val credits: Int = 50,
    val totalGenerations: Int = 0,
    val isAdmin: Boolean = true,
    val isGuest: Boolean = false,
    val avatarEmoji: String = "🎬"
)
