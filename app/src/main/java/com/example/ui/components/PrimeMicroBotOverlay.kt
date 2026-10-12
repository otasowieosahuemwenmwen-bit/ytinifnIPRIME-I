package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.AppLanguage
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.PrimeCyan
import com.example.ui.theme.PrimePink
import com.example.ui.theme.PrimeViolet

@Composable
fun PrimeMicroBotOverlay(
    selectedLanguage: AppLanguage,
    onExecuteCommand: (String) -> Unit,
    onOpenImageStudio: () -> Unit,
    onOpenTranscribe: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var detectedSpeech by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("Say 'Prime' followed by your command...") }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            feedbackText = "Speech recognizer unavailable on device. You can tap quick voice commands below."
            isListening = false
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.locale)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                feedbackText = "Listening for 'Prime' or your command..."
            }

            override fun onBeginningOfSpeech() {
                feedbackText = "Prime is hearing you..."
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
                feedbackText = "Executing command..."
            }

            override fun onError(error: Int) {
                isListening = false
                feedbackText = "Tap the glowing orb to speak again"
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    detectedSpeech = text
                    feedbackText = "Understood: \"$text\""
                    onExecuteCommand(text)
                }
                isListening = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    detectedSpeech = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            speechRecognizer.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            isListening = false
            feedbackText = "Tap to speak again"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            feedbackText = "Microphone access needed for Prime voice commands"
        }
    }

    // Auto-listen on dialog appear
    LaunchedEffect(Unit) {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPerm) {
            startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Siri-like Animated Orb Transformations
    val infiniteTransition = rememberInfiniteTransition(label = "siri_orb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Dialog(
        onDismissRequest = {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                // Ignore
            }
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .clickable { onDismiss() }
                .testTag("prime_microbot_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clickable(enabled = false) {}, // Prevent dismiss when tapping dialog content
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isListening) Color(0xFF10B981) else PrimeCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRIME MICRO BOT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimeCyan,
                            letterSpacing = 2.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_microbot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Prime",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Siri-Style Iridescent Pulsating Orb
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(if (isListening) pulseScale else 1f)
                        .clickable {
                            if (isListening) {
                                try {
                                    speechRecognizer?.stopListening()
                                } catch (e: Exception) {
                                    // Ignore
                                }
                                isListening = false
                            } else {
                                startListening()
                            }
                        }
                        .testTag("siri_orb_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Holographic Aura Glow
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        PrimeCyan.copy(alpha = glowAlpha * 0.45f),
                                        PrimeViolet.copy(alpha = glowAlpha * 0.35f),
                                        PrimePink.copy(alpha = glowAlpha * 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Middle Swirling Gradient Wave
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .rotate(rotateAngle)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        PrimeCyan,
                                        PrimeViolet,
                                        PrimePink,
                                        Color(0xFF3B82F6),
                                        PrimeCyan
                                    )
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    )

                    // Inner Core Orb with Infinity Icon
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                                )
                            )
                            .border(2.dp, PrimeCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Listening",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        } else {
                            Text(
                                text = "⚡",
                                fontSize = 36.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Spoken/Detected Prompt Display
                if (detectedSpeech.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, PrimeCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        color = Color(0xFF111827).copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "“$detectedSpeech”",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Feedback status
                Text(
                    text = feedbackText,
                    fontSize = 13.sp,
                    color = DarkTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Quick Voice Command Chips
                Text(
                    text = "Try saying or tapping:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val quickCommands = listOf(
                    "Prime, switch to Sports" to {
                        onExecuteCommand("switch to sports")
                        onDismiss()
                    },
                    "Prime, enable Private Mode" to {
                        onExecuteCommand("enable private mode")
                        onDismiss()
                    },
                    "Prime, switch to Schools" to {
                        onExecuteCommand("switch to schools")
                        onDismiss()
                    },
                    "Prime, switch to Anime" to {
                        onExecuteCommand("switch to anime")
                        onDismiss()
                    },
                    "Prime, switch to Business" to {
                        onExecuteCommand("switch to business")
                        onDismiss()
                    },
                    "Prime, switch to Beauty" to {
                        onExecuteCommand("switch to beauty")
                        onDismiss()
                    },
                    "Prime, change to Spanish" to {
                        onExecuteCommand("change to spanish")
                        onDismiss()
                    },
                    "Prime, change to Japanese" to {
                        onExecuteCommand("change to japanese")
                        onDismiss()
                    },
                    "Prime, start new chat" to {
                        onExecuteCommand("start new chat")
                        onDismiss()
                    },
                    "Prime, create anime image" to {
                        onOpenImageStudio()
                        onDismiss()
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickCommands.forEach { (label, action) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { action() }
                                .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                                .testTag("voice_cmd_$label"),
                            color = Color(0xFF1E293B).copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "💬 $label",
                                fontSize = 12.sp,
                                color = PrimeCyan,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
