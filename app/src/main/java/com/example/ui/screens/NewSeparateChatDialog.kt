package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryItem
import com.example.data.model.DefaultCategories
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.PrimeCyan
import com.example.ui.theme.PrimePink
import com.example.ui.theme.PrimeViolet

data class ChatTemplate(
    val title: String,
    val categoryId: String,
    val emoji: String,
    val description: String,
    val defaultPrompt: String
)

val AvailableChatTemplates = listOf(
    ChatTemplate("🎨 Image Creation Chat", "anime", "🎨", "Dedicated studio for creating & editing AI visual art", "Create a futuristic sci-fi city with neon reflections"),
    ChatTemplate("🎬 Video Creation Chat", "sports", "🎬", "Direct prompt-to-video scenes, camera pans & storyboard", "Generate a cinematic 4K drone flyover of alpine mountains"),
    ChatTemplate("💬 AI Assistant Chat", "schools", "⚡", "All-in-one everyday multi-turn conversational AI", "Hello Prime! What can you help me with today?"),
    ChatTemplate("💼 Business & Finance Chat", "businesses", "💼", "Market analysis, startup deck review & growth models", "Review my SaaS growth pitch and pricing strategy"),
    ChatTemplate("🌸 Anime & Manga Studio", "anime", "🌸", "Character design, dialogue writing & anime lore", "Design an epic magical warrior with lightning abilities"),
    ChatTemplate("✨ Beauty & Makeup Stylist", "beauty_makeup", "✨", "Skincare routine builder & cosmetic color matching", "Create a minimalist glowing skincare routine for morning")
)

@Composable
fun NewSeparateChatDialog(
    categories: List<CategoryItem>,
    onCreateSeparateChat: (title: String, categoryId: String, initialPrompt: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var chatTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: DefaultCategories.SCHOOLS) }
    var initialPrompt by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, CosmicBorder, RoundedCornerShape(24.dp))
                .testTag("new_separate_chat_dialog"),
            color = CosmicBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimeCyan, PrimeViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Add a Separate Chat",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Create distinct sessions for images, videos & tasks",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_new_chat_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Quick Templates
                Text(
                    text = "Select a Chat Blueprint:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AvailableChatTemplates.forEach { template ->
                        val isSelected = chatTitle == template.title
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    chatTitle = template.title
                                    initialPrompt = template.defaultPrompt
                                    val cat = categories.find { it.id == template.categoryId }
                                    if (cat != null) selectedCategory = cat
                                }
                                .border(
                                    1.dp,
                                    if (isSelected) PrimeCyan else CosmicBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .testTag("template_${template.categoryId}"),
                            color = if (isSelected) PrimeCyan.copy(alpha = 0.18f) else CosmicSurface
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = template.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = template.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkTextPrimary
                                    )
                                    Text(
                                        text = template.description,
                                        fontSize = 11.sp,
                                        color = DarkTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Custom Chat Title
                Text(
                    text = "Chat Name / Title:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = chatTitle,
                    onValueChange = { chatTitle = it },
                    placeholder = { Text("e.g. 🎨 Neon Art Studio, 🎬 YouTube Shorts Generator") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimeCyan,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select Persona Category
                Text(
                    text = "Persona & Domain:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = cat.id == selectedCategory.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat }
                                .border(
                                    1.dp,
                                    if (isSel) PrimeCyan else CosmicBorder,
                                    RoundedCornerShape(12.dp)
                                ),
                            color = if (isSel) PrimeCyan.copy(alpha = 0.2f) else CosmicSurface
                        ) {
                            Text(
                                text = "${cat.iconEmoji} ${cat.title}",
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else DarkTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Create Action Button
                Button(
                    onClick = {
                        val finalTitle = chatTitle.ifBlank { "${selectedCategory.iconEmoji} ${selectedCategory.title} Chat" }
                        onCreateSeparateChat(finalTitle, selectedCategory.id, initialPrompt.ifBlank { null })
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_create_chat_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimeCyan),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Separate Chat",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
