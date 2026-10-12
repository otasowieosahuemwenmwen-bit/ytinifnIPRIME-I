package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.data.model.CategoryItem
import com.example.data.model.ChatMessage
import com.example.ui.MainViewModel
import com.example.ui.components.CategoryHeader
import com.example.ui.components.FloatingMicroBotOrb
import com.example.ui.components.MessageBubble
import com.example.ui.components.PrimeMicroBotOverlay
import com.example.ui.components.PrimeWakeWordIndicator
import com.example.ui.components.PrivateModeBanner
import com.example.ui.components.SeparateChatsBar
import com.example.ui.screens.NewSeparateChatDialog
import com.example.ui.screens.OfflineModeDialog
import com.example.ui.screens.VideoStudioDialog
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.PrivateLockColor
import com.example.ui.theme.PrivateSurface
import com.example.ui.theme.PrimeCyan
import com.example.ui.theme.PrimePink
import com.example.ui.theme.PrimeViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val activeCategory by viewModel.activeCategory.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val activeConversation by viewModel.activeConversation.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val privateMessages by viewModel.privateMessages.collectAsStateWithLifecycle()
    val isPrivateMode by viewModel.isPrivateMode.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val speakingMessageId by viewModel.speakingMessageId.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val toastMsg by viewModel.uiToastMessage.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isOfflineUnlocked by viewModel.isOfflineUnlocked.collectAsStateWithLifecycle()
    val isOfflineActive by viewModel.isOfflineActive.collectAsStateWithLifecycle()
    val isWakeWordDetected by viewModel.isWakeWordDetected.collectAsStateWithLifecycle()
    val wakeWordSpeech by viewModel.wakeWordSpeech.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Dialog Visibility States
    var showCategoryManager by remember { mutableStateOf(false) }
    var showProfileManager by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showAudioTranscribe by remember { mutableStateOf(false) }
    var showImageStudio by remember { mutableStateOf(false) }
    var showVideoStudio by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showMicroBot by remember { mutableStateOf(false) }
    var showNewSeparateChat by remember { mutableStateOf(false) }
    var showOfflineDialog by remember { mutableStateOf(false) }

    val currentDisplayMessages = if (isPrivateMode) privateMessages else messages

    // Toast notification observer
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.dismissToast()
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(currentDisplayMessages.size, isGenerating) {
        if (currentDisplayMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentDisplayMessages.size - 1)
        }
    }

    val parsedCategoryColor = try {
        Color(android.graphics.Color.parseColor(activeCategory.colorHex))
    } catch (e: Exception) {
        PrimeViolet
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground),
        containerColor = CosmicBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showProfileManager = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(PrimeViolet, parsedCategoryColor)
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeProfile?.avatarEmoji ?: "⚡",
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Prime Infinity",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimeCyan,
                                    modifier = Modifier
                                        .background(PrimeCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = activeProfile?.name ?: "User Profile",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Private Mode Toggle Button with visible status pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.togglePrivateMode(!isPrivateMode) }
                            .border(
                                1.dp,
                                if (isPrivateMode) PrivateLockColor else CosmicBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .testTag("toggle_private_mode_button"),
                        color = if (isPrivateMode) PrivateLockColor.copy(alpha = 0.22f) else CosmicSurfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPrivateMode) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Toggle Private Mode",
                                tint = if (isPrivateMode) PrivateLockColor else DarkTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPrivateMode) "Private ON" else "Private",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPrivateMode) PrivateLockColor else DarkTextSecondary
                            )
                        }
                    }

                    // Conversation History
                    if (!isPrivateMode) {
                        IconButton(
                            onClick = { showHistory = true },
                            modifier = Modifier.testTag("open_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = DarkTextSecondary
                            )
                        }
                    }

                    // Offline Mode ($25 Lifetime Option)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showOfflineDialog = true }
                            .border(
                                1.dp,
                                if (isOfflineActive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                RoundedCornerShape(16.dp)
                            )
                            .testTag("offline_mode_topbar_button"),
                        color = if (isOfflineActive) Color(0xFF064E3B).copy(alpha = 0.55f) else Color(0xFF78350F).copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOfflineActive) Icons.Default.CloudOff else Icons.Default.Bolt,
                                contentDescription = "Offline Mode",
                                tint = if (isOfflineActive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isOfflineActive) "Offline ON" else "Offline $25",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOfflineActive) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }

                    // Video Creation Studio
                    IconButton(
                        onClick = { showVideoStudio = true },
                        modifier = Modifier.testTag("open_video_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Create Video",
                            tint = Color(0xFFF472B6)
                        )
                    }

                    // Image Creation Studio
                    IconButton(
                        onClick = { showImageStudio = true },
                        modifier = Modifier.testTag("open_image_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Create & Edit Images",
                            tint = PrimePink
                        )
                    }

                    // Separate Chat Quick Add
                    IconButton(
                        onClick = { showNewSeparateChat = true },
                        modifier = Modifier.testTag("open_new_separate_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Separate Chat",
                            tint = PrimeCyan
                        )
                    }

                    // Language Switcher
                    IconButton(
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier.testTag("open_language_button")
                    ) {
                        Text(
                            text = selectedLanguage.flag,
                            fontSize = 18.sp
                        )
                    }

                    // Micro Bot Siri Voice Assistant
                    IconButton(
                        onClick = { showMicroBot = true },
                        modifier = Modifier.testTag("open_microbot_topbar_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(PrimeCyan, PrimeViolet, PrimePink, PrimeCyan)
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚡", fontSize = 13.sp)
                        }
                    }

                    // Settings
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = DarkTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CosmicSurface,
                    titleContentColor = DarkTextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category & Persona Selector Header
            CategoryHeader(
                categories = allCategories,
                activeCategory = activeCategory,
                isPrivateMode = isPrivateMode,
                onCategorySelected = { viewModel.selectCategory(it) },
                onTogglePrivateMode = { viewModel.togglePrivateMode(it) },
                onOpenCategoryManager = { showCategoryManager = true }
            )

            // Separate Chats Horizontal Selector Bar
            if (!isPrivateMode) {
                SeparateChatsBar(
                    conversations = conversations,
                    activeConversation = activeConversation,
                    onSelectChat = { viewModel.selectConversation(it) },
                    onOpenNewChatDialog = { showNewSeparateChat = true },
                    onDeleteChat = { viewModel.deleteConversation(it) }
                )
            }

            // Private Mode Banner (if incognito is active)
            if (isPrivateMode) {
                PrivateModeBanner(
                    onClearSession = { viewModel.clearPrivateSession() },
                    onExitPrivateMode = { viewModel.togglePrivateMode(false) }
                )
            }

            // Chat Messages / Welcome Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                if (currentDisplayMessages.isEmpty()) {
                    // Empty State / Persona Greeting Card
                    CategoryWelcomeView(
                        category = activeCategory,
                        isPrivate = isPrivateMode,
                        onStarterSelected = { starterText ->
                            inputText = starterText
                            viewModel.sendMessage(starterText)
                            inputText = ""
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(currentDisplayMessages, key = { it.id }) { msg ->
                            val isThisMsgSpeaking = isSpeaking && speakingMessageId == msg.id
                            MessageBubble(
                                message = msg,
                                categoryEmoji = activeCategory.iconEmoji,
                                categoryColor = parsedCategoryColor,
                                isSpeaking = isThisMsgSpeaking,
                                onSpeakToggle = { viewModel.speakText(msg.content, msg.id) },
                                onRegenerate = { viewModel.regenerateLastMessage() },
                                onLikeToggle = { liked -> viewModel.toggleLikeMessage(msg, liked) },
                                onReplySelected = { reply ->
                                    viewModel.sendMessage(reply)
                                }
                            )
                        }

                        if (isGenerating) {
                            item {
                                GeneratingIndicator(
                                    categoryColor = parsedCategoryColor,
                                    categoryEmoji = activeCategory.iconEmoji
                                )
                            }
                        }
                    }
                }

                // Floating Siri-Style Micro Bot Orb
                FloatingMicroBotOrb(
                    onClick = { showMicroBot = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 6.dp)
                )
            }

            // Quick Starter Chips above input when idle and conversation started
            if (currentDisplayMessages.isNotEmpty() && !isGenerating) {
                val starters = activeCategory.getStarterPromptList()
                val starterScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(starterScrollState)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    starters.take(3).forEach { starter ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.sendMessage(starter)
                                }
                                .border(0.5.dp, CosmicBorder, RoundedCornerShape(12.dp)),
                            color = CosmicSurfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "💡 $starter",
                                fontSize = 11.sp,
                                color = DarkTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicSurface)
                    .border(width = 0.5.dp, color = CosmicBorder)
                    .imePadding()
                    .navigationBarsPadding(),
                color = CosmicSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (isPrivateMode) "🔒 Incognito message..." else "Ask ${activeCategory.title}...",
                                color = DarkTextMuted,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isPrivateMode) PrivateLockColor else parsedCategoryColor,
                            unfocusedBorderColor = CosmicBorder,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary,
                            cursorColor = if (isPrivateMode) PrivateLockColor else PrimeCyan
                        ),
                        shape = RoundedCornerShape(22.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !isGenerating) {
                                    val text = inputText
                                    inputText = ""
                                    keyboardController?.hide()
                                    viewModel.sendMessage(text)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Quick Audio Dictate button
                    IconButton(
                        onClick = { showAudioTranscribe = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceVariant.copy(alpha = 0.6f))
                            .testTag("quick_mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Dictate / Transcribe",
                            tint = if (isPrivateMode) PrivateLockColor else PrimeCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Quick Image Studio button
                    IconButton(
                        onClick = { showImageStudio = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceVariant.copy(alpha = 0.6f))
                            .testTag("quick_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Create / Edit Images",
                            tint = PrimePink,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Quick Video Studio button
                    IconButton(
                        onClick = { showVideoStudio = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceVariant.copy(alpha = 0.6f))
                            .testTag("quick_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Create / Animate Video",
                            tint = Color(0xFFF472B6),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank() && !isGenerating) {
                                    if (isPrivateMode) Brush.linearGradient(listOf(PrivateLockColor, Color(0xFF059669)))
                                    else Brush.linearGradient(listOf(PrimeViolet, parsedCategoryColor))
                                } else {
                                    Brush.linearGradient(listOf(CosmicSurfaceVariant, CosmicSurfaceVariant))
                                }
                            )
                            .clickable(enabled = inputText.isNotBlank() && !isGenerating) {
                                val text = inputText
                                inputText = ""
                                keyboardController?.hide()
                                viewModel.sendMessage(text)
                            }
                            .testTag("send_message_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) Color.White else DarkTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCategoryManager) {
        CategoryManagerDialog(
            categories = allCategories,
            activeCategory = activeCategory,
            onCategorySelected = { viewModel.selectCategory(it) },
            onAddCategory = { title, subtitle, emoji, color, tone, prompt, starters ->
                viewModel.addCustomCategory(title, subtitle, emoji, color, tone, prompt, starters)
            },
            onDeleteCategory = { viewModel.deleteCategory(it) },
            onDismiss = { showCategoryManager = false }
        )
    }

    if (showProfileManager) {
        ProfileManagerDialog(
            profiles = allProfiles,
            activeProfile = activeProfile,
            onProfileSelected = { viewModel.switchProfile(it) },
            onCreateProfile = { name, emoji, bio ->
                viewModel.createProfile(name, emoji, bio)
            },
            onDismiss = { showProfileManager = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            profile = activeProfile,
            customApiKey = customApiKey,
            currentLanguage = selectedLanguage,
            onOpenLanguageSelector = { showLanguageDialog = true },
            onSaveApiKey = { viewModel.saveCustomApiKey(it) },
            onSaveSettings = { name, emoji, bio, temp, tone, len, instructions, tts ->
                viewModel.updateProfileSettings(name, emoji, bio, temp, tone, len, instructions, tts)
            },
            onDismiss = { showSettings = false }
        )
    }

    if (showHistory) {
        HistoryDialog(
            conversations = conversations,
            activeConversation = activeConversation,
            onSelectConversation = { viewModel.selectConversation(it) },
            onStartNewConversation = { viewModel.startNewConversation() },
            onDeleteConversation = { viewModel.deleteConversation(it) },
            onDismiss = { showHistory = false }
        )
    }

    if (showAudioTranscribe) {
        AudioTranscribeDialog(
            activeCategory = activeCategory,
            selectedLanguage = selectedLanguage,
            onSendTranscriptToChat = { transcript ->
                viewModel.sendMessage(transcript)
            },
            onDismiss = { showAudioTranscribe = false }
        )
    }

    if (showImageStudio) {
        ImageStudioDialog(
            activeCategory = activeCategory,
            onSendImageToChat = { title, note ->
                viewModel.sendMessage("🎨 Visual Creation: $title\n$note")
            },
            onDismiss = { showImageStudio = false }
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = selectedLanguage,
            onLanguageSelected = { lang ->
                viewModel.setLanguage(lang)
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showMicroBot) {
        PrimeMicroBotOverlay(
            selectedLanguage = selectedLanguage,
            onExecuteCommand = { cmd ->
                viewModel.executeVoiceCommand(cmd)
            },
            onOpenImageStudio = {
                showImageStudio = true
            },
            onOpenTranscribe = {
                showAudioTranscribe = true
            },
            onDismiss = { showMicroBot = false }
        )
    }

    if (showNewSeparateChat) {
        NewSeparateChatDialog(
            categories = allCategories,
            onCreateSeparateChat = { title, catId, initialPrompt ->
                viewModel.createCustomSeparateChat(title, catId, initialPrompt)
            },
            onDismiss = { showNewSeparateChat = false }
        )
    }

    if (showOfflineDialog) {
        OfflineModeDialog(
            isOfflineUnlocked = isOfflineUnlocked,
            isOfflineActive = isOfflineActive,
            onPurchaseOffline = {
                viewModel.purchaseOfflineMode()
            },
            onToggleOfflineActive = { active ->
                viewModel.toggleOfflineActive(active)
            },
            onDismiss = { showOfflineDialog = false }
        )
    }

    if (showVideoStudio) {
        VideoStudioDialog(
            activeCategory = activeCategory,
            onSendVideoToChat = { title, prompt, duration, style ->
                viewModel.sendMessage("🎬 $title\n\nPrompt: $prompt\nStyle: $style • Duration: $duration\n[Keyframes rendered]")
            },
            onDismiss = { showVideoStudio = false }
        )
    }

    // Wake Word Visual Overlay Indicator
    PrimeWakeWordIndicator(
        isVisible = isWakeWordDetected,
        recognizedText = wakeWordSpeech,
        onDismiss = { viewModel.triggerWakeWordIndicator(false) },
        onQuickCommand = { cmd ->
            viewModel.triggerWakeWordIndicator(false)
            when (cmd) {
                "create video" -> showVideoStudio = true
                "create image" -> showImageStudio = true
                "offline mode" -> showOfflineDialog = true
                "new separate chat" -> showNewSeparateChat = true
                else -> viewModel.executeVoiceCommand(cmd)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryWelcomeView(
    category: CategoryItem,
    isPrivate: Boolean,
    onStarterSelected: (String) -> Unit
) {
    val parsedColor = if (isPrivate) {
        PrivateLockColor
    } else {
        try {
            Color(android.graphics.Color.parseColor(category.colorHex))
        } catch (e: Exception) {
            PrimeViolet
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(parsedColor.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
                .border(1.5.dp, parsedColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (isPrivate) "🔒" else category.iconEmoji, fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isPrivate) "Private AI Confidential Chat" else category.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isPrivate) "Zero-Log Ephemeral Session • Messages Never Saved" else category.subtitle,
            fontSize = 13.sp,
            color = DarkTextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
            color = parsedColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = if (isPrivate) "Security: Volatile RAM Only" else "Persona Tone: ${category.toneLabel}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = parsedColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isPrivate) "Confidential Starter Prompts:" else "Suggested Questions & Prompts:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkTextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        val starters = if (isPrivate) {
            listOf(
                "🔒 Confidential inquiry without saving to history",
                "🔒 Sensitive brainstorm for private personal thoughts",
                "🔒 Review proprietary code or confidential document",
                "🔒 Private negotiation strategy & scenario planning"
            )
        } else {
            category.getStarterPromptList()
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            starters.forEach { prompt ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onStarterSelected(prompt) }
                        .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                        .testTag("starter_prompt_$prompt"),
                    color = CosmicSurface
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = parsedColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = prompt,
                            fontSize = 13.sp,
                            color = DarkTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratingIndicator(categoryColor: Color, categoryEmoji: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "generating_pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = categoryEmoji, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp)),
            color = CosmicSurface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prime Infinity is formulating response",
                    fontSize = 12.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier.alpha(alphaAnim)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = categoryColor,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
