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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.PrimeViolet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryManagerDialog(
    categories: List<CategoryItem>,
    activeCategory: CategoryItem,
    onCategorySelected: (CategoryItem) -> Unit,
    onAddCategory: (
        title: String,
        subtitle: String,
        iconEmoji: String,
        colorHex: String,
        toneLabel: String,
        systemPrompt: String,
        starterPrompts: String
    ) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }

    // Form inputs
    var newTitle by remember { mutableStateOf("") }
    var newSubtitle by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("🔮") }
    var newColorHex by remember { mutableStateOf("#8B5CF6") }
    var newTone by remember { mutableStateOf("Specialized & Insightful") }
    var newSystemPrompt by remember { mutableStateOf("") }
    var newStarterPrompts by remember { mutableStateOf("") }

    val presetColors = listOf(
        "#8B5CF6", "#3B82F6", "#10B981", "#F59E0B",
        "#EC4899", "#06B6D4", "#F43F5E", "#EAB308"
    )

    val presetEmojis = listOf("🔮", "🚀", "💡", "🧠", "🎮", "🧪", "🎨", "🌿", "🧘", "📈", "🎵", "🍳")

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
                .testTag("category_manager_dialog"),
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
                            text = if (showAddForm) "Create New Category" else "Category & Persona Hub",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                        Text(
                            text = if (showAddForm) "Customize tone, prompt & feeling" else "Personalized conversation spaces",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            if (showAddForm) showAddForm = false else onDismiss()
                        },
                        modifier = Modifier.testTag("close_category_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showAddForm) {
                    // Add Category Form
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Category Title") },
                                placeholder = { Text("e.g. Culinary Arts, Game Dev, History") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_category_title_input"),
                                colors = outlinedFieldColors(),
                                singleLine = true
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = newSubtitle,
                                onValueChange = { newSubtitle = it },
                                label = { Text("Subtitle / Purpose") },
                                placeholder = { Text("e.g. Recipes, wine pairings & kitchen tips") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = outlinedFieldColors(),
                                singleLine = true
                            )
                        }

                        item {
                            Text(
                                text = "Choose Icon Emoji",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                presetEmojis.forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
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
                                        Text(text = emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Accent Theme Color",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                presetColors.forEach { hex ->
                                    val col = try {
                                        Color(android.graphics.Color.parseColor(hex))
                                    } catch (e: Exception) {
                                        PrimeViolet
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                if (newColorHex == hex) 2.dp else 0.dp,
                                                Color.White,
                                                CircleShape
                                            )
                                            .clickable { newColorHex = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (newColorHex == hex) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = newTone,
                                onValueChange = { newTone = it },
                                label = { Text("Tone & Persona Style") },
                                placeholder = { Text("e.g. Socratic, Hyper-Enthusiastic, Direct") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = outlinedFieldColors(),
                                singleLine = true
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = newSystemPrompt,
                                onValueChange = { newSystemPrompt = it },
                                label = { Text("Custom AI Instructions (System Prompt)") },
                                placeholder = { Text("Tell Prime Infinity how to behave, respond, and what knowledge to prioritize...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                colors = outlinedFieldColors(),
                                maxLines = 4
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = newStarterPrompts,
                                onValueChange = { newStarterPrompts = it },
                                label = { Text("Suggested Starter Prompts (separate with ||)") },
                                placeholder = { Text("Prompt 1||Prompt 2||Prompt 3") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = outlinedFieldColors(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddForm = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextSecondary)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    val prompt = if (newSystemPrompt.isNotBlank()) {
                                        newSystemPrompt
                                    } else {
                                        "You are the Prime Infinity specialist for $newTitle. Maintain a $newTone tone."
                                    }
                                    val starters = if (newStarterPrompts.isNotBlank()) {
                                        newStarterPrompts
                                    } else {
                                        "What are the basics of $newTitle?||Give me 3 expert tips||Help me create a project plan"
                                    }
                                    onAddCategory(
                                        newTitle,
                                        newSubtitle.ifBlank { "Personalized assistant" },
                                        newEmoji,
                                        newColorHex,
                                        newTone,
                                        prompt,
                                        starters
                                    )
                                    showAddForm = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_new_category_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimeViolet),
                            enabled = newTitle.isNotBlank()
                        ) {
                            Text("Create Mode", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Category List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat.id == activeCategory.id
                            val parsedColor = try {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            } catch (e: Exception) {
                                PrimeViolet
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        onCategorySelected(cat)
                                        onDismiss()
                                    }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) parsedColor else CosmicBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .testTag("manage_cat_item_${cat.id}"),
                                color = if (isSelected) parsedColor.copy(alpha = 0.15f) else CosmicSurface
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
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(parsedColor.copy(alpha = 0.25f))
                                                .border(1.dp, parsedColor.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = cat.iconEmoji, fontSize = 22.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = cat.title,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DarkTextPrimary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                                    color = if (cat.isBuiltIn) CosmicSurfaceVariant else parsedColor.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = if (cat.isBuiltIn) "Built-in" else "Custom",
                                                        fontSize = 10.sp,
                                                        color = if (cat.isBuiltIn) DarkTextMuted else parsedColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = cat.subtitle,
                                                fontSize = 12.sp,
                                                color = DarkTextSecondary
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Tone: ${cat.toneLabel}",
                                                fontSize = 11.sp,
                                                color = parsedColor
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Active",
                                                tint = parsedColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        if (!cat.isBuiltIn) {
                                            IconButton(
                                                onClick = { onDeleteCategory(cat.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showAddForm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_custom_category_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimeViolet),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Custom Category", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimeCyan,
    unfocusedBorderColor = CosmicBorder,
    focusedLabelColor = PrimeCyan,
    unfocusedLabelColor = DarkTextSecondary,
    focusedTextColor = DarkTextPrimary,
    unfocusedTextColor = DarkTextPrimary
)
