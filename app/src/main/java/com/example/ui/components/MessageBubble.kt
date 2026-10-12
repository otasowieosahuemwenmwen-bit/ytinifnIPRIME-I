package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceVariant
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.PrimeCyan
import com.example.ui.theme.PrimeViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    categoryEmoji: String,
    categoryColor: Color,
    isSpeaking: Boolean,
    onSpeakToggle: () -> Unit,
    onRegenerate: () -> Unit,
    onLikeToggle: (Boolean) -> Unit,
    onReplySelected: (String) -> Unit
) {
    val context = LocalContext.current
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag(if (isUser) "user_message_${message.id}" else "ai_message_${message.id}"),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                // AI Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(categoryColor, PrimeViolet)
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = categoryEmoji,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Bubble Card
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isUser) {
                            Brush.linearGradient(
                                listOf(
                                    PrimeViolet.copy(alpha = 0.85f),
                                    categoryColor.copy(alpha = 0.85f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    CosmicSurface,
                                    CosmicSurfaceVariant
                                )
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (isUser) categoryColor.copy(alpha = 0.5f) else CosmicBorder,
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                // Sender label & time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "You" else "Prime Infinity",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUser) Color.White.copy(alpha = 0.9f) else categoryColor
                    )
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = if (isUser) Color.White.copy(alpha = 0.7f) else DarkTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Message Text Content
                FormattedMessageText(text = message.content, isUser = isUser)

                // Action Bar for AI Messages
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // TTS read aloud
                        IconButton(
                            onClick = onSpeakToggle,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("tts_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isSpeaking) "Stop voice" else "Read aloud",
                                tint = if (isSpeaking) PrimeCyan else DarkTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Copy to clipboard
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Prime Infinity Message", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("copy_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = DarkTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Share
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, message.content)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share AI Response"))
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("share_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = DarkTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Like
                        IconButton(
                            onClick = { onLikeToggle(true) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Helpful",
                                tint = if (message.isLiked == true) PrimeCyan else DarkTextSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Dislike
                        IconButton(
                            onClick = { onLikeToggle(false) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "Not helpful",
                                tint = if (message.isLiked == false) Color(0xFFEF4444) else DarkTextSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Regenerate
                        IconButton(
                            onClick = onRegenerate,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("regenerate_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                tint = DarkTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimeCyan.copy(alpha = 0.2f))
                        .border(1.dp, PrimeCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        fontSize = 18.sp
                    )
                }
            }
        }

        // Suggested Smart Replies
        val replies = message.getRepliesList()
        if (!isUser && replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp, top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                replies.forEach { reply ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onReplySelected(reply) }
                            .border(1.dp, categoryColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .testTag("smart_reply_$reply"),
                        color = CosmicSurfaceVariant.copy(alpha = 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💬 ",
                                fontSize = 11.sp
                            )
                            Text(
                                text = reply,
                                fontSize = 12.sp,
                                color = DarkTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedMessageText(text: String, isUser: Boolean) {
    val textColor = if (isUser) Color.White else DarkTextPrimary

    // Parse simple code blocks or bullet lists
    val blocks = text.split("```")
    if (blocks.size > 1) {
        Column {
            blocks.forEachIndexed { index, block ->
                if (index % 2 == 1) {
                    // Code block
                    val lines = block.trim().split("\n")
                    val lang = if (lines.isNotEmpty() && lines[0].matches(Regex("^[a-zA-Z0-9]+$"))) lines[0] else ""
                    val code = if (lang.isNotEmpty()) lines.drop(1).joinToString("\n") else block

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, CosmicBorder, RoundedCornerShape(8.dp)),
                        color = Color(0xFF070B13)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (lang.isNotEmpty()) {
                                Text(
                                    text = lang.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimeCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = code.trim(),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                } else if (block.isNotBlank()) {
                    Text(
                        text = block.trim(),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = textColor
                    )
                }
            }
        }
    } else {
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = textColor
        )
    }
}
