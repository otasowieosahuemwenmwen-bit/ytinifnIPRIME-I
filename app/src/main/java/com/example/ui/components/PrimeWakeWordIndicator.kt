package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.PrimeCyan
import com.example.ui.theme.PrimePink
import com.example.ui.theme.PrimeViolet

@Composable
fun PrimeWakeWordIndicator(
    isVisible: Boolean,
    recognizedText: String,
    onDismiss: () -> Unit,
    onQuickCommand: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wake_word_pulse")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("wake_word_overlay_container"),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Screen Perimeter Holographic Aura Glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                PrimeCyan.copy(alpha = borderGlow),
                                PrimePink.copy(alpha = borderGlow),
                                PrimeViolet.copy(alpha = borderGlow),
                                PrimeCyan.copy(alpha = borderGlow)
                            )
                        ),
                        shape = RectangleShape
                    )
            )

            // Dynamic Island / Bottom HUD Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(listOf(PrimeCyan, PrimePink)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("wake_word_hud_card"),
                color = Color(0xFF0F172A).copy(alpha = 0.96f),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsating Live Orb
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(waveScale)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(PrimeCyan, PrimePink, PrimeViolet, PrimeCyan)
                                        )
                                    )
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "⚡ \"PRIME\" WAKE-WORD DETECTED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimeCyan,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "Listening for your command...",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("dismiss_wake_indicator")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = DarkTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio Soundwave Animation Bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val heights = listOf(14.dp, 26.dp, 36.dp, 20.dp, 32.dp, 28.dp, 16.dp, 30.dp, 18.dp)
                        heights.forEachIndexed { index, baseHeight ->
                            val dynamicHeight = (baseHeight.value * (0.6f + (waveScale * 0.4f))).dp
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(dynamicHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (index % 2 == 0) PrimeCyan else PrimePink
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Speech Display
                    if (recognizedText.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = Color(0xFF1E293B).copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "“$recognizedText”",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Quick Command Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val chips = listOf(
                            "Create Video" to "create video",
                            "Create Image" to "create image",
                            "Offline Mode ($25)" to "offline mode",
                            "New Separate Chat" to "new separate chat",
                            "Private Mode" to "enable private mode",
                            "Switch to Sports" to "switch to sports",
                            "Switch to Anime" to "switch to anime"
                        )

                        chips.forEach { (label, cmd) ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onQuickCommand(cmd) }
                                    .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp)),
                                color = CosmicBorder.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = "⚡ $label",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimeCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
