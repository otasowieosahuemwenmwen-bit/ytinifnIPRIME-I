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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
fun ProfileManagerDialog(
    profiles: List<UserProfile>,
    activeProfile: UserProfile?,
    onProfileSelected: (UserProfile) -> Unit,
    onCreateProfile: (name: String, emoji: String, bio: String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newBio by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("🌟") }

    val presetEmojis = listOf("⚡", "🚀", "🌟", "👑", "🔮", "🕶️", "🎨", "🦁", "🐉", "🤖")

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
                .testTag("profile_manager_dialog"),
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
                    Column {
                        Text(
                            text = if (showCreateForm) "New User Profile" else "Profiles & Personas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                        Text(
                            text = if (showCreateForm) "Create your personal AI persona profile" else "Switch profile to adapt the AI to your identity",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            if (showCreateForm) showCreateForm = false else onDismiss()
                        },
                        modifier = Modifier.testTag("close_profile_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showCreateForm) {
                    // Create Profile Form
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Profile Name") },
                            placeholder = { Text("e.g. Alex (Study Mode), Phoenix, Jordan") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_profile_name_input"),
                            colors = outlinedFieldColors(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newBio,
                            onValueChange = { newBio = it },
                            label = { Text("Bio / Focus Area") },
                            placeholder = { Text("e.g. Preparing for medical exams & marathon training") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedFieldColors(),
                            singleLine = true
                        )

                        Text(
                            text = "Avatar Emoji",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextSecondary
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetEmojis.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (newEmoji == emoji) PrimeViolet.copy(alpha = 0.4f)
                                            else CosmicSurfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (newEmoji == emoji) PrimeViolet else CosmicBorder,
                                            CircleShape
                                        )
                                        .clickable { newEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCreateForm = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextSecondary)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    if (newName.isNotBlank()) {
                                        onCreateProfile(
                                            newName,
                                            newEmoji,
                                            newBio.ifBlank { "Personalized Prime Infinity User" }
                                        )
                                        showCreateForm = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_profile_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimeViolet),
                                enabled = newName.isNotBlank()
                            ) {
                                Text("Create Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Profile List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(profiles) { profile ->
                            val isSelected = profile.id == activeProfile?.id

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        onProfileSelected(profile)
                                        onDismiss()
                                    }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PrimeCyan else CosmicBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .testTag("profile_item_${profile.id}"),
                                color = if (isSelected) PrimeCyan.copy(alpha = 0.12f) else CosmicSurface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(PrimeViolet.copy(alpha = 0.25f))
                                                .border(1.dp, PrimeViolet.copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = profile.avatarEmoji, fontSize = 24.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = profile.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkTextPrimary
                                            )
                                            Text(
                                                text = profile.bio,
                                                fontSize = 12.sp,
                                                color = DarkTextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Tone: ${profile.tone} • Temp: ${profile.temperature}",
                                                fontSize = 11.sp,
                                                color = DarkTextMuted
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active Profile",
                                            tint = PrimeCyan,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showCreateForm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_profile_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimeCyan),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Profile", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}
