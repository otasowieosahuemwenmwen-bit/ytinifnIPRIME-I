package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarEmoji: String = "⚡",
    val bio: String = "AI Explorer",
    val activeCategoryId: String = "schools",
    val temperature: Float = 0.7f,
    val tone: String = "Helpful & Balanced",
    val responseLength: String = "Balanced",
    val customInstructions: String = "",
    val ttsEnabled: Boolean = false,
    val isPrivateByDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
