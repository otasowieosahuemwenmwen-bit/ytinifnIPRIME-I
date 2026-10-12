package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.data.model.AppLanguage
import com.example.data.model.UserProfile
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.PrimeCyan
import com.example.ui.theme.PrimeViolet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    profile: UserProfile?,
    customApiKey: String,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onOpenLanguageSelector: () -> Unit = {},
    onSaveApiKey: (String) -> Unit,
    onSaveSettings: (
        name: String,
        emoji: String,
        bio: String,
        temperature: Float,
        tone: String,
        responseLength: String,
        customInstructions: String,
        ttsEnabled: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val currentProfile = profile ?: return

    var name by remember { mutableStateOf(currentProfile.name) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var temperature by remember { mutableFloatStateOf(currentProfile.temperature) }
    var selectedTone by remember { mutableStateOf(currentProfile.tone) }
    var selectedLength by remember { mutableStateOf(currentProfile.responseLength) }
    var customInstructions by remember { mutableStateOf(currentProfile.customInstructions) }
    var ttsEnabled by remember { mutableStateOf(currentProfile.ttsEnabled) }
    var apiKeyInput by remember { mutableStateOf(customApiKey) }

    val tones = listOf(
        "Helpful & Balanced",
        "Academic & Methodical",
        "Creative & Playful",
        "Concise & Direct",
        "Empathetic Mentor"
    )

    val lengths = listOf("Concise", "Balanced", "Detailed")

    val hasBuildConfigKey = try {
        BuildConfig.GEMINI_API_KEY.isNotBlank() && !BuildConfig.GEMINI_API_KEY.contains("MY_GEMINI_API_KEY")
    } catch (e: Throwable) {
        false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
                .testTag("settings_dialog"),
            color = CosmicBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = PrimeCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Customization & Settings",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Personality & Tone
                    item {
                        SectionCard(title = "AI Tone & Demeanor") {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tones.forEach { tone ->
                                    val isSelected = tone == selectedTone
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedTone = tone }
                                            .border(
                                                1.dp,
                                                if (isSelected) PrimeCyan else CosmicBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .testTag("tone_option_$tone"),
                                        color = if (isSelected) PrimeCyan.copy(alpha = 0.2f) else CosmicSurfaceVariant
                                    ) {
                                        Text(
                                            text = tone,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else DarkTextSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Creativity / Temperature
                    item {
                        SectionCard(title = "Creativity & Temperature: ${String.format("%.1f", temperature)}") {
                            Column {
                                Slider(
                                    value = temperature,
                                    onValueChange = { temperature = it },
                                    valueRange = 0.0f..1.0f,
                                    steps = 9,
                                    colors = SliderDefaults.colors(
                                        thumbColor = PrimeCyan,
                                        activeTrackColor = PrimeViolet,
                                        inactiveTrackColor = CosmicBorder
                                    ),
                                    modifier = Modifier.testTag("temperature_slider")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("0.0 (Precise / Factual)", fontSize = 11.sp, color = DarkTextMuted)
                                    Text("1.0 (Creative / Wild)", fontSize = 11.sp, color = DarkTextMuted)
                                }
                            }
                        }
                    }

                    // Response Length
                    item {
                        SectionCard(title = "Response Length Preference") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                lengths.forEach { len ->
                                    val isSelected = len == selectedLength
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { selectedLength = len }
                                            .border(
                                                1.dp,
                                                if (isSelected) PrimeViolet else CosmicBorder,
                                                RoundedCornerShape(10.dp)
                                            ),
                                        color = if (isSelected) PrimeViolet.copy(alpha = 0.25f) else CosmicSurfaceVariant
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = len,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else DarkTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Custom System Instructions
                    item {
                        SectionCard(title = "Custom AI Instructions") {
                            OutlinedTextField(
                                value = customInstructions,
                                onValueChange = { customInstructions = it },
                                placeholder = { Text("e.g. Always include 3 bullet points, cite sources, or explain using sports metaphors...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .testTag("custom_instructions_input"),
                                colors = outlinedFieldColors(),
                                maxLines = 3
                            )
                        }
                    }

                    // Language Localization
                    item {
                        SectionCard(title = "Language & Localization") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onOpenLanguageSelector() }
                                    .border(1.dp, PrimeCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .background(CosmicSurfaceVariant)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = currentLanguage.flag, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentLanguage.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = DarkTextPrimary
                                        )
                                        Text(
                                            text = currentLanguage.nativeName,
                                            fontSize = 11.sp,
                                            color = DarkTextSecondary
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                                    color = PrimeCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Change",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimeCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Text to Speech
                    item {
                        SectionCard(title = "Audio & Speech") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto Read Aloud (TTS)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkTextPrimary
                                    )
                                    Text(
                                        text = "Automatically speak responses using Android speech engine",
                                        fontSize = 11.sp,
                                        color = DarkTextSecondary
                                    )
                                }

                                Switch(
                                    checked = ttsEnabled,
                                    onCheckedChange = { ttsEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PrimeCyan,
                                        checkedTrackColor = PrimeViolet
                                    ),
                                    modifier = Modifier.testTag("tts_switch")
                                )
                            }
                        }
                    }

                    // Gemini API Key Override
                    item {
                        SectionCard(title = "Gemini AI API Key Configuration") {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = if (hasBuildConfigKey) PrimeCyan else DarkTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (hasBuildConfigKey) "Active key detected via BuildConfig" else "Using local engine (or enter custom key below)",
                                        fontSize = 12.sp,
                                        color = if (hasBuildConfigKey) PrimeCyan else DarkTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = apiKeyInput,
                                    onValueChange = {
                                        apiKeyInput = it
                                        onSaveApiKey(it)
                                    },
                                    label = { Text("Custom Gemini API Key Override (Optional)") },
                                    placeholder = { Text("AIzaSy...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("api_key_input"),
                                    colors = outlinedFieldColors(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onSaveSettings(
                            name,
                            currentProfile.avatarEmoji,
                            bio,
                            temperature,
                            selectedTone,
                            selectedLength,
                            customInstructions,
                            ttsEnabled
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_settings_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimeViolet),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Apply & Save Preferences", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp)),
        color = CosmicSurface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimeCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
