package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiService
import com.example.data.local.AppDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.CategoryItem
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.DefaultCategories
import com.example.data.model.UserProfile
import com.example.data.repository.PrimeInfinityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PrimeInfinityRepository
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    // State flows
    val allProfiles: StateFlow<List<UserProfile>>
    val allCategories: StateFlow<List<CategoryItem>>

    private val _activeProfile = MutableStateFlow<UserProfile?>(null)
    val activeProfile: StateFlow<UserProfile?> = _activeProfile.asStateFlow()

    private val _activeCategory = MutableStateFlow<CategoryItem>(DefaultCategories.SCHOOLS)
    val activeCategory: StateFlow<CategoryItem> = _activeCategory.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _activeConversation = MutableStateFlow<Conversation?>(null)
    val activeConversation: StateFlow<Conversation?> = _activeConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Private mode ephemeral storage
    private val _isPrivateMode = MutableStateFlow(false)
    val isPrivateMode: StateFlow<Boolean> = _isPrivateMode.asStateFlow()

    private val _privateMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val privateMessages: StateFlow<List<ChatMessage>> = _privateMessages.asStateFlow()

    // UI generation states
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Speech states
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    private val _uiToastMessage = MutableStateFlow<String?>(null)
    val uiToastMessage: StateFlow<String?> = _uiToastMessage.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    // Offline Mode states ($25 lifetime option)
    private val _isOfflineUnlocked = MutableStateFlow(false)
    val isOfflineUnlocked: StateFlow<Boolean> = _isOfflineUnlocked.asStateFlow()

    private val _isOfflineActive = MutableStateFlow(false)
    val isOfflineActive: StateFlow<Boolean> = _isOfflineActive.asStateFlow()

    // Wake Word Visual Overlay states
    private val _isWakeWordDetected = MutableStateFlow(false)
    val isWakeWordDetected: StateFlow<Boolean> = _isWakeWordDetected.asStateFlow()

    private val _wakeWordSpeech = MutableStateFlow("")
    val wakeWordSpeech: StateFlow<String> = _wakeWordSpeech.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = PrimeInfinityRepository(db.appDao(), GeminiApiService())

        allProfiles = repository.allProfiles.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        allCategories = repository.allCategories.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DefaultCategories.ALL
        )

        initTts(application)

        // Observe profiles to select default
        viewModelScope.launch {
            allProfiles.collectLatest { profiles ->
                if (profiles.isNotEmpty() && _activeProfile.value == null) {
                    val initialUser = profiles.first()
                    _activeProfile.value = initialUser
                    loadCategoryForUser(initialUser.activeCategoryId)
                }
            }
        }

        // Observe category updates
        viewModelScope.launch {
            allCategories.collectLatest { categories ->
                val current = _activeCategory.value
                val matched = categories.find { it.id == current.id }
                if (matched != null) {
                    _activeCategory.value = matched
                }
            }
        }
    }

    private fun initTts(application: Application) {
        try {
            tts = TextToSpeech(application) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    isTtsInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED
                }
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to init TTS: ${e.message}")
        }
    }

    fun dismissToast() {
        _uiToastMessage.value = null
    }

    fun showToast(msg: String) {
        _uiToastMessage.value = msg
    }

    // --- Profile Operations ---
    fun switchProfile(profile: UserProfile) {
        _activeProfile.value = profile
        loadCategoryForUser(profile.activeCategoryId)
        if (_isPrivateMode.value) {
            clearPrivateSession()
        }
        showToast("Switched to profile: ${profile.name}")
    }

    fun createProfile(name: String, emoji: String, bio: String) {
        viewModelScope.launch {
            val currentCategory = _activeCategory.value.id
            val newProfile = repository.createProfile(name, emoji, bio, currentCategory)
            _activeProfile.value = newProfile
            showToast("Created profile '${name}'")
        }
    }

    fun updateProfileSettings(
        name: String,
        emoji: String,
        bio: String,
        temperature: Float,
        tone: String,
        responseLength: String,
        customInstructions: String,
        ttsEnabled: Boolean
    ) {
        val current = _activeProfile.value ?: return
        val updated = current.copy(
            name = name,
            avatarEmoji = emoji,
            bio = bio,
            temperature = temperature,
            tone = tone,
            responseLength = responseLength,
            customInstructions = customInstructions,
            ttsEnabled = ttsEnabled
        )
        _activeProfile.value = updated
        viewModelScope.launch {
            repository.updateProfile(updated)
            showToast("Settings updated successfully")
        }
    }

    // --- Category Operations ---
    private fun loadCategoryForUser(categoryId: String) {
        viewModelScope.launch {
            val cat = allCategories.value.find { it.id == categoryId }
                ?: DefaultCategories.ALL.find { it.id == categoryId }
                ?: DefaultCategories.SCHOOLS
            selectCategory(cat)
        }
    }

    fun selectCategory(category: CategoryItem) {
        _activeCategory.value = category
        val currentProfile = _activeProfile.value
        if (currentProfile != null && currentProfile.activeCategoryId != category.id) {
            val updated = currentProfile.copy(activeCategoryId = category.id)
            _activeProfile.value = updated
            viewModelScope.launch {
                repository.updateProfile(updated)
            }
        }

        // If not in private mode, reload conversation for this category
        if (!_isPrivateMode.value) {
            loadConversationForActiveCategory()
        }
    }

    fun addCustomCategory(
        title: String,
        subtitle: String,
        iconEmoji: String,
        colorHex: String,
        toneLabel: String,
        systemPrompt: String,
        starterPrompts: String
    ) {
        viewModelScope.launch {
            val userId = _activeProfile.value?.id ?: "system"
            val newCat = repository.addCategory(
                userId = userId,
                title = title,
                subtitle = subtitle,
                iconEmoji = iconEmoji,
                colorHex = colorHex,
                toneLabel = toneLabel,
                systemPrompt = systemPrompt,
                starterPrompts = starterPrompts
            )
            selectCategory(newCat)
            showToast("Category '$title' created!")
        }
    }

    fun updateCategory(category: CategoryItem) {
        viewModelScope.launch {
            repository.updateCategory(category)
            if (_activeCategory.value.id == category.id) {
                _activeCategory.value = category
            }
            showToast("Category updated")
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
            if (_activeCategory.value.id == categoryId) {
                selectCategory(DefaultCategories.SCHOOLS)
            }
            showToast("Category deleted")
        }
    }

    // --- Conversation Operations ---
    private fun loadConversationForActiveCategory() {
        val user = _activeProfile.value ?: return
        val category = _activeCategory.value

        viewModelScope.launch {
            repository.getConversationsForCategory(user.id, category.id).collectLatest { convs ->
                _conversations.value = convs
                if (convs.isNotEmpty()) {
                    if (_activeConversation.value?.categoryId != category.id ||
                        convs.none { it.id == _activeConversation.value?.id }
                    ) {
                        selectConversation(convs.first())
                    }
                } else {
                    // Create default conversation for category
                    val newConv = repository.createConversation(
                        userId = user.id,
                        categoryId = category.id,
                        title = "${category.iconEmoji} ${category.title} Chat"
                    )
                    _activeConversation.value = newConv
                    observeMessagesForConversation(newConv.id)
                }
            }
        }
    }

    fun selectConversation(conversation: Conversation) {
        _activeConversation.value = conversation
        observeMessagesForConversation(conversation.id)
    }

    fun startNewConversation() {
        val user = _activeProfile.value ?: return
        val category = _activeCategory.value
        viewModelScope.launch {
            val newConv = repository.createConversation(
                userId = user.id,
                categoryId = category.id,
                title = "${category.iconEmoji} ${category.title} #${(100..999).random()}"
            )
            _activeConversation.value = newConv
            observeMessagesForConversation(newConv.id)
            showToast("Started new chat thread")
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
            showToast("Chat deleted")
        }
    }

    fun createCustomSeparateChat(title: String, categoryId: String, initialPrompt: String? = null) {
        val user = _activeProfile.value ?: return
        viewModelScope.launch {
            val cat = allCategories.value.find { it.id == categoryId }
                ?: DefaultCategories.ALL.find { it.id == categoryId }
                ?: DefaultCategories.SCHOOLS
            selectCategory(cat)

            val newConv = repository.createConversation(
                userId = user.id,
                categoryId = categoryId,
                title = title
            )
            _activeConversation.value = newConv
            observeMessagesForConversation(newConv.id)
            showToast("Separate Chat '$title' opened!")

            if (!initialPrompt.isNullOrBlank()) {
                delay(250)
                sendMessage(initialPrompt)
            }
        }
    }

    // --- Offline Mode ($25 Lifetime License) ---
    fun purchaseOfflineMode() {
        _isOfflineUnlocked.value = true
        _isOfflineActive.value = true
        showToast("🎉 $25 Lifetime Offline License Activated! Running 100% on-device.")
    }

    fun toggleOfflineActive(active: Boolean) {
        if (!_isOfflineUnlocked.value && active) {
            showToast("Offline Mode requires the $25 lifetime license")
            return
        }
        _isOfflineActive.value = active
        if (active) {
            showToast("🟢 Switched to 100% On-Device Offline AI Engine")
        } else {
            showToast("🌐 Switched to Cloud Gemini AI Engine")
        }
    }

    // --- Wake Word Visual Overlay ---
    fun triggerWakeWordIndicator(active: Boolean) {
        _isWakeWordDetected.value = active
        if (!active) {
            _wakeWordSpeech.value = ""
        }
    }

    fun updateWakeWordSpeech(speech: String) {
        _wakeWordSpeech.value = speech
    }

    private fun observeMessagesForConversation(conversationId: String) {
        viewModelScope.launch {
            repository.getMessages(conversationId).collectLatest { msgList ->
                _messages.value = msgList
            }
        }
    }

    // --- Private Mode (Strictly In-Memory) ---
    fun togglePrivateMode(enable: Boolean) {
        _isPrivateMode.value = enable
        if (enable) {
            _privateMessages.value = emptyList()
            showToast("🔒 Incognito Private Session started (Messages are never stored)")
        } else {
            clearPrivateSession()
            loadConversationForActiveCategory()
            showToast("Exited Private Session")
        }
    }

    fun clearPrivateSession() {
        _privateMessages.value = emptyList()
        stopSpeaking()
    }

    // --- Sending Messages & AI Reply ---
    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isGenerating.value) return
        val trimmedPrompt = userText.trim()
        val category = _activeCategory.value
        val profile = _activeProfile.value
        val temp = profile?.temperature ?: 0.7f
        val effectiveApiKey = if (_isOfflineActive.value) "OFFLINE_LOCAL_ENGINE" else _customApiKey.value.ifBlank { null }

        stopSpeaking()

        if (_isPrivateMode.value) {
            // Private Mode: Add user message in memory only
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = "private_temp",
                sender = "USER",
                content = trimmedPrompt,
                categoryId = category.id,
                isPrivate = true
            )
            _privateMessages.value = _privateMessages.value + userMsg

            viewModelScope.launch {
                _isGenerating.value = true
                try {
                    val (aiText, replies) = repository.generateAiReply(
                        userPrompt = trimmedPrompt,
                        history = _privateMessages.value,
                        category = category,
                        temperature = temp,
                        customApiKey = effectiveApiKey,
                        languageName = _selectedLanguage.value.displayName
                    )

                    val aiMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        conversationId = "private_temp",
                        sender = "AI",
                        content = aiText,
                        suggestedReplies = replies.joinToString("||"),
                        categoryId = category.id,
                        isPrivate = true
                    )
                    _privateMessages.value = _privateMessages.value + aiMsg

                    if (profile?.ttsEnabled == true) {
                        speakText(aiText, aiMsg.id)
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Private generation failed: ${e.message}")
                } finally {
                    _isGenerating.value = false
                }
            }
        } else {
            // Persistent Mode
            val conversation = _activeConversation.value ?: return
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = conversation.id,
                sender = "USER",
                content = trimmedPrompt,
                categoryId = category.id,
                isPrivate = false
            )

            viewModelScope.launch {
                repository.saveMessage(userMsg)
                repository.updateConversation(conversation.copy(updatedAt = System.currentTimeMillis()))
                _isGenerating.value = true

                try {
                    val currentHistory = _messages.value
                    val (aiText, replies) = repository.generateAiReply(
                        userPrompt = trimmedPrompt,
                        history = currentHistory,
                        category = category,
                        temperature = temp,
                        customApiKey = effectiveApiKey,
                        languageName = _selectedLanguage.value.displayName
                    )

                    val aiMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        conversationId = conversation.id,
                        sender = "AI",
                        content = aiText,
                        suggestedReplies = replies.joinToString("||"),
                        categoryId = category.id,
                        isPrivate = false
                    )
                    repository.saveMessage(aiMsg)

                    if (profile?.ttsEnabled == true) {
                        speakText(aiText, aiMsg.id)
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "AI generation error: ${e.message}")
                } finally {
                    _isGenerating.value = false
                }
            }
        }
    }

    fun regenerateLastMessage() {
        val lastUserMessage = if (_isPrivateMode.value) {
            _privateMessages.value.findLast { it.sender == "USER" }
        } else {
            _messages.value.findLast { it.sender == "USER" }
        } ?: return

        sendMessage(lastUserMessage.content)
    }

    fun toggleLikeMessage(message: ChatMessage, liked: Boolean) {
        val updated = message.copy(isLiked = if (message.isLiked == liked) null else liked)
        if (_isPrivateMode.value) {
            _privateMessages.value = _privateMessages.value.map {
                if (it.id == message.id) updated else it
            }
        } else {
            viewModelScope.launch {
                repository.updateMessage(updated)
            }
        }
    }

    // --- Speech TTS ---
    fun speakText(text: String, messageId: String) {
        if (!isTtsInitialized || tts == null) {
            showToast("Text-to-speech engine initializing...")
            return
        }

        if (_isSpeaking.value && _speakingMessageId.value == messageId) {
            stopSpeaking()
            return
        }

        try {
            tts?.stop()
            val cleanText = text
                .replace("*", "")
                .replace("#", "")
                .replace("`", "")
                .take(600) // Read reasonable snippet
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, messageId)
            _isSpeaking.value = true
            _speakingMessageId.value = messageId
        } catch (e: Exception) {
            Log.e("MainViewModel", "TTS speak failed: ${e.message}")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        _speakingMessageId.value = null
    }

    fun saveCustomApiKey(key: String) {
        _customApiKey.value = key.trim()
        showToast("Gemini API key updated")
    }

    fun setLanguage(lang: AppLanguage) {
        _selectedLanguage.value = lang
        try {
            tts?.setLanguage(lang.locale)
        } catch (e: Exception) {
            // ignore
        }
        showToast("Language set to ${lang.flag} ${lang.displayName}")
    }

    // --- Micro Bot Siri-like Voice Command Interpreter ---
    fun executeVoiceCommand(rawSpeech: String) {
        val cleanSpeech = rawSpeech.trim()
        if (cleanSpeech.isBlank()) return

        // Strip wake phrases: "prime", "hey prime", "hi prime", "ok prime"
        val command = cleanSpeech
            .replace(Regex("^(hey|hi|ok|hello)?\\s*prime[\\s,:]*", RegexOption.IGNORE_CASE), "")
            .trim()
        val lower = command.lowercase()

        when {
            // Private / Incognito Mode
            lower.contains("private") || lower.contains("incognito") || lower.contains("secret") -> {
                if (lower.contains("exit") || lower.contains("disable") || lower.contains("off") || lower.contains("stop")) {
                    togglePrivateMode(false)
                    speakText("Exited private mode. Returning to normal chat.", "microbot_cmd")
                } else {
                    togglePrivateMode(true)
                    speakText("Private confidential mode enabled. Zero logs active.", "microbot_cmd")
                }
            }

            // Category Modes
            lower.contains("school") || lower.contains("academic") || lower.contains("study") || lower.contains("math") -> {
                val cat = allCategories.value.find { it.id == "schools" } ?: DefaultCategories.SCHOOLS
                togglePrivateMode(false)
                selectCategory(cat)
                speakText("Switched to Schools and Academic mode.", "microbot_cmd")
            }

            lower.contains("sport") || lower.contains("workout") || lower.contains("fitness") || lower.contains("train") -> {
                val cat = allCategories.value.find { it.id == "sports" } ?: DefaultCategories.SPORTS
                togglePrivateMode(false)
                selectCategory(cat)
                speakText("Switched to Sports and Athletics mode. Let's train!", "microbot_cmd")
            }

            lower.contains("business") || lower.contains("startup") || lower.contains("finance") || lower.contains("money") -> {
                val cat = allCategories.value.find { it.id == "businesses" } ?: DefaultCategories.BUSINESSES
                togglePrivateMode(false)
                selectCategory(cat)
                speakText("Switched to Business and Executive mode.", "microbot_cmd")
            }

            lower.contains("anime") || lower.contains("manga") || lower.contains("otaku") -> {
                val cat = allCategories.value.find { it.id == "anime" } ?: DefaultCategories.ANIME
                togglePrivateMode(false)
                selectCategory(cat)
                speakText("Switched to Anime and Otaku mode. Konnichiwa!", "microbot_cmd")
            }

            lower.contains("beauty") || lower.contains("makeup") || lower.contains("skincare") -> {
                val cat = allCategories.value.find { it.id == "beauty_makeup" } ?: DefaultCategories.BEAUTY_MAKEUP
                togglePrivateMode(false)
                selectCategory(cat)
                speakText("Switched to Beauty and Skincare mode.", "microbot_cmd")
            }

            // Language Changes
            lower.contains("spanish") || lower.contains("español") -> {
                setLanguage(AppLanguage.SPANISH)
                speakText("Idioma cambiado a Español.", "microbot_cmd")
            }

            lower.contains("french") || lower.contains("français") -> {
                setLanguage(AppLanguage.FRENCH)
                speakText("Langue changée en Français.", "microbot_cmd")
            }

            lower.contains("german") || lower.contains("deutsch") -> {
                setLanguage(AppLanguage.GERMAN)
                speakText("Sprache auf Deutsch umgestellt.", "microbot_cmd")
            }

            lower.contains("japanese") || lower.contains("nihongo") -> {
                setLanguage(AppLanguage.JAPANESE)
                speakText("言語を日本語に切り替えました。", "microbot_cmd")
            }

            lower.contains("english") -> {
                setLanguage(AppLanguage.ENGLISH)
                speakText("Language switched to English.", "microbot_cmd")
            }

            // Offline Mode Voice Command
            lower.contains("offline") -> {
                if (lower.contains("exit") || lower.contains("disable") || lower.contains("off")) {
                    toggleOfflineActive(false)
                    speakText("Exited offline mode. Reconnected to cloud AI.", "microbot_cmd")
                } else {
                    if (_isOfflineUnlocked.value) {
                        toggleOfflineActive(true)
                        speakText("Offline AI mode activated. Running 100% on-device.", "microbot_cmd")
                    } else {
                        speakText("Offline AI requires the 25 dollar lifetime license. Opening offline activation.", "microbot_cmd")
                    }
                }
            }

            // Separate Chat Command
            lower.contains("separate chat") || lower.contains("new chat") -> {
                createCustomSeparateChat("⚡ Separate Chat #${(100..999).random()}", _activeCategory.value.id)
                speakText("Opened a separate chat session.", "microbot_cmd")
            }

            // AI Question / Query: send directly to Gemini and speak back response
            command.isNotBlank() -> {
                sendMessage(command)
                speakText("Asking Prime Infinity AI...", "microbot_cmd")
            }

            else -> {
                speakText("I'm listening. Try saying: Prime, switch to Sports, or Prime, go incognito.", "microbot_cmd")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignore on shutdown
        }
    }
}
