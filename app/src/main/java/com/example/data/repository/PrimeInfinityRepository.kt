package com.example.data.repository

import com.example.data.api.GeminiApiService
import com.example.data.local.AppDao
import com.example.data.model.CategoryItem
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PrimeInfinityRepository(
    private val appDao: AppDao,
    private val geminiApiService: GeminiApiService
) {
    // --- Profiles ---
    val allProfiles: Flow<List<UserProfile>> = appDao.getAllProfiles()

    suspend fun getProfileById(id: String): UserProfile? = appDao.getProfileById(id)

    suspend fun createProfile(name: String, avatarEmoji: String, bio: String, activeCategory: String): UserProfile {
        val newProfile = UserProfile(
            name = name,
            avatarEmoji = avatarEmoji,
            bio = bio,
            activeCategoryId = activeCategory
        )
        appDao.insertProfile(newProfile)
        return newProfile
    }

    suspend fun updateProfile(profile: UserProfile) = appDao.updateProfile(profile)

    suspend fun deleteProfile(profileId: String) = appDao.deleteProfileById(profileId)

    // --- Categories ---
    val allCategories: Flow<List<CategoryItem>> = appDao.getAllCategories()

    suspend fun addCategory(
        userId: String,
        title: String,
        subtitle: String,
        iconEmoji: String,
        colorHex: String,
        toneLabel: String,
        systemPrompt: String,
        starterPrompts: String
    ): CategoryItem {
        val category = CategoryItem(
            userId = userId,
            title = title,
            subtitle = subtitle,
            iconEmoji = iconEmoji,
            colorHex = colorHex,
            toneLabel = toneLabel,
            systemPrompt = systemPrompt,
            starterPrompts = starterPrompts,
            isBuiltIn = false
        )
        appDao.insertCategory(category)
        return category
    }

    suspend fun updateCategory(category: CategoryItem) = appDao.updateCategory(category)

    suspend fun deleteCategory(categoryId: String) = appDao.deleteCategoryById(categoryId)

    // --- Conversations ---
    fun getConversations(userId: String): Flow<List<Conversation>> =
        appDao.getConversationsForUser(userId)

    fun getConversationsForCategory(userId: String, categoryId: String): Flow<List<Conversation>> =
        appDao.getConversationsForCategory(userId, categoryId)

    suspend fun createConversation(userId: String, categoryId: String, title: String): Conversation {
        val conversation = Conversation(
            userId = userId,
            categoryId = categoryId,
            title = title,
            isPrivate = false
        )
        appDao.insertConversation(conversation)
        return conversation
    }

    suspend fun updateConversation(conversation: Conversation) = appDao.updateConversation(conversation)

    suspend fun deleteConversation(conversationId: String) {
        appDao.deleteMessagesForConversation(conversationId)
        appDao.deleteConversationById(conversationId)
    }

    // --- Messages ---
    fun getMessages(conversationId: String): Flow<List<ChatMessage>> =
        appDao.getMessagesForConversation(conversationId)

    suspend fun saveMessage(message: ChatMessage) = appDao.insertMessage(message)

    suspend fun updateMessage(message: ChatMessage) = appDao.updateMessage(message)

    suspend fun generateAiReply(
        userPrompt: String,
        history: List<ChatMessage>,
        category: CategoryItem,
        temperature: Float,
        customApiKey: String?,
        languageName: String = "English"
    ): Pair<String, List<String>> {
        return geminiApiService.generateResponse(
            userPrompt = userPrompt,
            conversationHistory = history,
            systemInstructionText = category.systemPrompt,
            temperature = temperature,
            customApiKey = customApiKey,
            languageName = languageName
        )
    }
}
