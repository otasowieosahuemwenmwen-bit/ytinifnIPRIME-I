package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.AppLanguage
import com.example.data.model.CategoryItem
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceVariant
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.PrimeCyan
import com.example.ui.theme.PrimeViolet
import java.util.Locale

@Composable
fun AudioTranscribeDialog(
    activeCategory: CategoryItem,
    selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    onSendTranscriptToChat: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var transcriptText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Tap the microphone below to start live transcription (${selectedLanguage.flag} ${selectedLanguage.displayName})") }

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
                // Ignore cleanup
            }
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            statusMessage = "Speech recognizer unavailable, using mock transcription"
            transcriptText = "This is a recorded voice transcription analyzing ${activeCategory.title} strategy and action points for Prime Infinity."
            isRecording = false
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.locale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLanguage.code)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, selectedLanguage.code)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                statusMessage = "Listening... Speak clearly into your microphone"
                isRecording = true
            }

            override fun onBeginningOfSpeech() {
                statusMessage = "Detecting speech..."
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                statusMessage = "Processing speech to text..."
                isRecording = false
            }

            override fun onError(error: Int) {
                isRecording = false
                statusMessage = "Audio input received. You can also edit or type manually."
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val result = matches[0]
                    transcriptText = if (transcriptText.isBlank()) result else "$transcriptText $result"
                    statusMessage = "Transcription complete!"
                }
                isRecording = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val partial = matches[0]
                    transcriptText = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            speechRecognizer.startListening(intent)
            isRecording = true
        } catch (e: Exception) {
            isRecording = false
            statusMessage = "Could not start audio listener: ${e.message}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
        isRecording = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            statusMessage = "Microphone permission required for audio transcription"
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_recording")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = {
            stopListening()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
                .testTag("audio_transcribe_dialog"),
            color = CosmicBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = PrimeCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Audio Transcriber",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Transcribe speech & voice notes for ${activeCategory.title}",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            stopListening()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_audio_transcribe_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Microphone Waveform Animation
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording) {
                                Brush.radialGradient(
                                    listOf(PrimeCyan.copy(alpha = 0.5f), Color.Transparent)
                                )
                            } else {
                                Brush.radialGradient(
                                    listOf(PrimeViolet.copy(alpha = 0.3f), Color.Transparent)
                                )
                            }
                        )
                        .scale(if (isRecording) pulseScale else 1f)
                        .clickable {
                            if (isRecording) {
                                stopListening()
                            } else {
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
                        }
                        .testTag("mic_record_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRecording) PrimeCyan else PrimeViolet
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop Recording" else "Record Audio",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isRecording) "🔴 Recording in progress..." else "Tap to Record / Transcribe",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) PrimeCyan else DarkTextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = DarkTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Transcript Result Box
                OutlinedTextField(
                    value = transcriptText,
                    onValueChange = { transcriptText = it },
                    label = { Text("Transcribed Text") },
                    placeholder = { Text("Voice transcription will appear here. You can also edit it...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("transcript_result_field"),
                    colors = outlinedFieldColors(),
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Presets / Starters for Audio Dictation
                Text(
                    text = "Or load a sample voice memo to analyze:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                val sampleMemos = when (activeCategory.id) {
                    "schools" -> "Professor lecture note: The key theorems of Thermodynamics state that total entropy of an isolated system always increases. Can you summarize this?"
                    "sports" -> "Workout log: Did 4 sets of back squats at 225 lbs, then 3 sets of Bulgarian split squats. Knees felt slightly stiff. What mobility drills should I add?"
                    "businesses" -> "Executive memo: Our customer acquisition cost increased by 15% this quarter, but lifetime value expanded to $1,200. What is our net unit economics status?"
                    "anime" -> "Review dictation: Just watched the season finale of Jujutsu Kaisen. The animation during the Shibuya arc was unreal. Compare the Shibuya incident to the Chimera Ant arc in Hunter x Hunter."
                    "beauty_makeup" -> "Skincare check: I just introduced a 10% niacinamide serum and mild retinol on alternate nights. Is it safe to use AHA/BHA chemical exfoliant on Sundays?"
                    else -> "Voice note: Please analyze this discussion, organize key points into bullet items, and suggest actionable next steps."
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { transcriptText = sampleMemos }
                        .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp)),
                    color = CosmicSurfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎙️ ", fontSize = 14.sp)
                        Text(
                            text = sampleMemos,
                            fontSize = 11.sp,
                            color = DarkTextSecondary,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy
                    OutlinedButton(
                        onClick = {
                            if (transcriptText.isNotBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Prime Infinity Transcript", transcriptText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Transcript copied", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextSecondary),
                        enabled = transcriptText.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy")
                    }

                    // Send to AI
                    Button(
                        onClick = {
                            if (transcriptText.isNotBlank()) {
                                onSendTranscriptToChat(transcriptText)
                                stopListening()
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("send_transcript_to_chat_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimeViolet),
                        enabled = transcriptText.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send to AI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
