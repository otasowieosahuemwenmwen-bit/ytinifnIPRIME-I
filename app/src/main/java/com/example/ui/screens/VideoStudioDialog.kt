package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryItem
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.PrimeCyan
import com.example.ui.theme.PrimePink
import com.example.ui.theme.PrimeViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

data class VideoPreset(
    val id: String,
    val name: String,
    val emoji: String,
    val motionDescription: String,
    val colors: List<Color>
)

val VideoPresets = listOf(
    VideoPreset("cinematic", "Cinematic 4K", "🎬", "Smooth Anamorphic Drone Glide", listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF38BDF8))),
    VideoPreset("anime", "Anime Studio", "🌸", "Dynamic High-Speed Shonen Sweep", listOf(Color(0xFF831843), Color(0xFFDB2777), Color(0xFFF472B6))),
    VideoPreset("cyberpunk", "Neon Cyberpunk", "⚡", "Futuristic Rain Orbit with Lens Flare", listOf(Color(0xFF022C22), Color(0xFF059669), Color(0xFF34D399))),
    VideoPreset("hyperlapse", "Hyper-Lapse", "⏳", "Rapid Fast-Forward Motion Blur", listOf(Color(0xFF311042), Color(0xFF7E22CE), Color(0xFFA855F7))),
    VideoPreset("cgi_unreal", "Unreal Engine 5", "🎮", "Volumetric Photorealistic Dolly Shot", listOf(Color(0xFF451A03), Color(0xFFD97706), Color(0xFFFBBF24)))
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoStudioDialog(
    activeCategory: CategoryItem,
    onSendVideoToChat: (title: String, prompt: String, duration: String, style: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var promptText by remember {
        mutableStateOf(
            when (activeCategory.id) {
                "sports" -> "Hyper-realistic slow-motion sprint finish under stadium floodlights, dynamic camera dolly tracking athlete"
                "businesses" -> "Sleek architectural drone tour of futuristic glass corporate headquarters at sunset"
                "anime" -> "Electric sword battle atop neon skyscraper in rainy neo-Tokyo with sakura petals swirling"
                "beauty_makeup" -> "Close-up cinematic beauty lighting revealing iridescent makeup transformation"
                else -> "Cosmic journey flying past rings of Saturn with volumetric stellar nebula particles"
            }
        )
    }

    var selectedPreset by remember { mutableStateOf(VideoPresets[0]) }
    var selectedAspectRatio by remember { mutableStateOf("16:9") } // "16:9", "9:16", "1:1"
    var durationSeconds by remember { mutableIntStateOf(5) } // 5s, 10s, 15s
    var motionIntensity by remember { mutableFloatStateOf(0.7f) }
    var cameraMovement by remember { mutableStateOf("Drone Flyover") }

    var isGeneratingVideo by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var playheadProgress by remember { mutableFloatStateOf(0f) }
    var hasGeneratedVideo by remember { mutableStateOf(true) }

    // Playback loop simulation
    LaunchedEffect(isPlaying, isGeneratingVideo) {
        while (isPlaying && !isGeneratingVideo) {
            delay(50)
            playheadProgress = (playheadProgress + 0.015f) % 1.0f
        }
    }

    // Motion animation
    val infiniteTransition = rememberInfiniteTransition(label = "video_motion")
    val animatedPan by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pan"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, CosmicBorder, RoundedCornerShape(24.dp))
                .testTag("video_studio_dialog"),
            color = CosmicBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
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
                                .background(Brush.linearGradient(listOf(PrimePink, PrimeViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Video Creation Studio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Generate cinematic motion clips & storyboards",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_video_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Video Player Canvas Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            when (selectedAspectRatio) {
                                "9:16" -> 9f / 14f
                                "1:1" -> 1f
                                else -> 16f / 9f
                            }
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.5.dp, PrimePink.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .clickable { isPlaying = !isPlaying }
                        .testTag("video_player_preview"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGeneratingVideo) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = PrimePink,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Synthesizing Neural Keyframes...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "${selectedPreset.name} • $cameraMovement",
                                fontSize = 11.sp,
                                color = PrimeCyan
                            )
                        }
                    } else {
                        // Dynamic Animated Canvas Rendering Simulated Motion
                        val primaryColor = selectedPreset.colors.getOrElse(1) { PrimePink }
                        val secondaryColor = selectedPreset.colors.getOrElse(2) { PrimeCyan }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val centerX = w / 2 + animatedPan * motionIntensity
                            val centerY = h / 2

                            // Background gradient
                            drawRect(
                                brush = Brush.radialGradient(
                                    listOf(secondaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0.7f), Color.Black),
                                    center = Offset(centerX, centerY),
                                    radius = w * 0.8f
                                )
                            )

                            // Animated motion rings & horizon line
                            rotate(playheadProgress * 360f, pivot = Offset(centerX, centerY)) {
                                drawCircle(
                                    brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor, primaryColor)),
                                    radius = w * 0.28f,
                                    center = Offset(centerX, centerY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                                )
                            }

                            // Starburst motion lines
                            for (i in 0..12) {
                                val angle = (i * 30f + playheadProgress * 90f) * (Math.PI / 180f).toFloat()
                                val startR = w * 0.15f
                                val endR = w * 0.45f
                                drawLine(
                                    color = secondaryColor.copy(alpha = 0.4f),
                                    start = Offset(centerX + startR * kotlin.math.cos(angle), centerY + startR * sin(angle)),
                                    end = Offset(centerX + endR * kotlin.math.cos(angle), centerY + endR * sin(angle)),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }

                        // Video HUD Overlay
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                    color = Color.Black.copy(alpha = 0.65f)
                                ) {
                                    Text(
                                        text = "LIVE RENDER • ${selectedPreset.emoji} ${selectedPreset.name}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                    color = Color.Black.copy(alpha = 0.65f)
                                ) {
                                    Text(
                                        text = "${selectedAspectRatio} • ${durationSeconds}s 60fps",
                                        fontSize = 10.sp,
                                        color = PrimeCyan,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Center Play / Pause Indicator
                            if (!isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            // Bottom Scrubber Bar
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "00:${String.format("%02d", (playheadProgress * durationSeconds).toInt())}",
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "00:${String.format("%02d", durationSeconds)}",
                                        fontSize = 10.sp,
                                        color = DarkTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(playheadProgress)
                                            .height(4.dp)
                                            .background(Brush.horizontalGradient(listOf(PrimeCyan, PrimePink)))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prompt Input
                Text(
                    text = "AI Video Prompt:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = { Text("Describe scenes, camera actions, lighting, and transitions...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_prompt_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimePink,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Style Preset Selector
                Text(
                    text = "Cinematic Style Presets:",
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
                    VideoPresets.forEach { preset ->
                        val isSelected = preset.id == selectedPreset.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedPreset = preset }
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PrimePink else CosmicBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("video_preset_${preset.id}"),
                            color = if (isSelected) PrimePink.copy(alpha = 0.2f) else CosmicSurface
                        ) {
                            Text(
                                text = "${preset.emoji} ${preset.name}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else DarkTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Controls: Aspect Ratio & Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Aspect Ratio
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aspect Ratio:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("16:9", "9:16", "1:1").forEach { ratio ->
                                val isSel = ratio == selectedAspectRatio
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedAspectRatio = ratio }
                                        .border(
                                            1.dp,
                                            if (isSel) PrimeCyan else CosmicBorder,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    color = if (isSel) PrimeCyan.copy(alpha = 0.2f) else CosmicSurface
                                ) {
                                    Text(
                                        text = ratio,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else DarkTextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Duration
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clip Duration:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(5, 10, 15).forEach { dur ->
                                val isSel = dur == durationSeconds
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { durationSeconds = dur }
                                        .border(
                                            1.dp,
                                            if (isSel) PrimePink else CosmicBorder,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    color = if (isSel) PrimePink.copy(alpha = 0.2f) else CosmicSurface
                                ) {
                                    Text(
                                        text = "${dur}s",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else DarkTextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Movement Selector
                Text(
                    text = "Camera Motion Preset: $cameraMovement",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Drone Flyover", "Orbital 360", "Dolly Zoom In", "Fast Pan Left", "Static Bokeh").forEach { cam ->
                        val isSel = cam == cameraMovement
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { cameraMovement = cam }
                                .border(1.dp, if (isSel) PrimeCyan else CosmicBorder, RoundedCornerShape(10.dp)),
                            color = if (isSel) PrimeCyan.copy(alpha = 0.18f) else CosmicSurface
                        ) {
                            Text(
                                text = cam,
                                fontSize = 11.sp,
                                color = if (isSel) PrimeCyan else DarkTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Generate / Render Clip Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isGeneratingVideo = true
                                isPlaying = false
                                delay(1800) // Simulate neural generation
                                isGeneratingVideo = false
                                isPlaying = true
                                playheadProgress = 0f
                                Toast.makeText(context, "AI Video Render Complete!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("render_video_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimePink),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isGeneratingVideo && promptText.isNotBlank()
                    ) {
                        if (isGeneratingVideo) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Render Video",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Send to Chat Button
                    Button(
                        onClick = {
                            val videoTitle = "🎬 ${selectedPreset.name} Video (${durationSeconds}s, ${selectedAspectRatio})"
                            onSendVideoToChat(videoTitle, promptText, "${durationSeconds}s", selectedPreset.name)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("send_video_chat_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimeCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send to Chat",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
