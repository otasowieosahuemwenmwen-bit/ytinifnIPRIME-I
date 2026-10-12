package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val sender: String, // "USER" or "AI"
    val content: String,
    val suggestedReplies: String = "", // Pipe-separated string of quick replies
    val timestamp: Long = System.currentTimeMillis(),
    val categoryId: String = "schools",
    val isPrivate: Boolean = false,
    val isLiked: Boolean? = null
) {
    fun getRepliesList(): List<String> {
        if (suggestedReplies.isBlank()) return emptyList()
        return suggestedReplies.split("||").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
