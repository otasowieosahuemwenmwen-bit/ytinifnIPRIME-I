package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.PrimePink
import com.example.ui.theme.PrimeViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class DrawingPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageStudioDialog(
    activeCategory: CategoryItem,
    onSendImageToChat: (prompt: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0: Create AI Image, 1: Edit & Canvas
    var promptInput by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Cyberpunk Neon") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var isGeneratingImage by remember { mutableStateOf(false) }

    // Generated image artwork state
    var generatedImageTitle by remember { mutableStateOf("") }
    var hasGeneratedImage by remember { mutableStateOf(false) }

    // Image Editing States
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var selectedFilterPreset by remember { mutableStateOf("Normal") }
    var captionText by remember { mutableStateOf("") }

    // Drawing / Doodle paths
    val paths = remember { mutableStateListOf<DrawingPath>() }
    var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var brushColor by remember { mutableStateOf(PrimeCyan) }
    var brushSize by remember { mutableFloatStateOf(8f) }
    var isDrawMode by remember { mutableStateOf(false) }

    val styles = listOf(
        "Cyberpunk Neon", "Anime / Manga", "Hyper-Realistic",
        "3D Digital Render", "Oil Painting", "Minimalist Vector"
    )

    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3")

    val filterPresets = listOf("Normal", "Cyber Glow", "Monochrome", "Warm Film", "Vibrant Gold", "Vaporwave")

    val brushColors = listOf(
        PrimeCyan, PrimeViolet, PrimePink,
        Color(0xFF10B981), Color(0xFFF59E0B), Color.White, Color.Black
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
                .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
                .testTag("image_studio_dialog"),
            color = CosmicBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = PrimePink,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Image Creation & Studio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Generate AI imagery & edit with filters and doodle markup",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_image_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher (0: Create Image, 1: Edit & Canvas)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CosmicSurface,
                    contentColor = PrimeCyan,
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(3.dp)
                                .background(PrimeCyan, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate AI Image", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit & Markup Studio", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // TAB 0: GENERATE AI IMAGE
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            label = { Text("Image Prompt Description") },
                            placeholder = { Text("e.g. Futuristic neural network infinity loop with glowing cyber particles...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("image_prompt_input"),
                            colors = outlinedFieldColors(),
                            maxLines = 4
                        )

                        // Category Inspirations
                        Text(
                            text = "Inspirations for ${activeCategory.title}:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextSecondary
                        )

                        val inspirations = when (activeCategory.id) {
                            "schools" -> listOf("3D Anatomical Heart with Glowing Arteries", "Astrophysics Spiral Galaxy Diagram", "Ancient Library of Alexandria in Unreal Engine")
                            "sports" -> listOf("Dynamic Sprinter Crossing Finish Line in Neon", "Basketball Slam Dunk with Shockwave FX", "Formula 1 Racing Night Track in Cyberpunk Tokyo")
                            "businesses" -> listOf("Glassmorphism SaaS Analytics Dashboard", "Modern Minimalist Geometric Startup Logo", "Venture Capital Skyscraper at Sunset")
                            "anime" -> listOf("Cyberpunk Mech Pilot Girl with Katana in Neo-Tokyo", "Chibi Magical Animal Companion with Sparkles", "Epic Shonen Energy Beam Clashing on Mountain")
                            "beauty_makeup" -> listOf("Luminous Glass Skin Glow Portrait in Golden Hour", "High-End Rose Gold Cosmetic Flacon on Marble", "Avant-Garde Futuristic Neon Eyeliner Art")
                            else -> listOf("Prime Infinity AI Neural Node Grid", "Luminous Hologram in Deep Cosmic Space", "Digital Synthwave Horizon")
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            inspirations.forEach { idea ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { promptInput = idea }
                                        .border(0.5.dp, CosmicBorder, RoundedCornerShape(10.dp)),
                                    color = CosmicSurfaceVariant
                                ) {
                                    Text(
                                        text = "✨ $idea",
                                        fontSize = 11.sp,
                                        color = DarkTextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Style Presets
                        Text(
                            text = "Aesthetic Style Preset:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            styles.forEach { style ->
                                val isSelected = style == selectedStyle
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedStyle = style }
                                        .border(
                                            1.dp,
                                            if (isSelected) PrimeCyan else CosmicBorder,
                                            RoundedCornerShape(12.dp)
                                        ),
                                    color = if (isSelected) PrimeCyan.copy(alpha = 0.2f) else CosmicSurface
                                ) {
                                    Text(
                                        text = style,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else DarkTextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Aspect Ratio
                        Text(
                            text = "Aspect Ratio:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            aspectRatios.forEach { ratio ->
                                val isSelected = ratio == selectedAspectRatio
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedAspectRatio = ratio }
                                        .border(
                                            1.dp,
                                            if (isSelected) PrimePink else CosmicBorder,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    color = if (isSelected) PrimePink.copy(alpha = 0.2f) else CosmicSurface
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ratio,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else DarkTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Generate Button
                        Button(
                            onClick = {
                                if (promptInput.isNotBlank()) {
                                    isGeneratingImage = true
                                    coroutineScope.launch {
                                        delay(1200) // Synthesizing visual rendering
                                        generatedImageTitle = promptInput
                                        hasGeneratedImage = true
                                        isGeneratingImage = false
                                        selectedTab = 1 // Switch to edit studio to view and edit!
                                        Toast.makeText(context, "Artwork generated! Opening Editor Studio", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_image_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimeViolet),
                            shape = RoundedCornerShape(14.dp),
                            enabled = promptInput.isNotBlank() && !isGeneratingImage
                        ) {
                            if (isGeneratingImage) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing Visual Art...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate AI Image ($selectedStyle)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // TAB 1: EDIT & MARKUP STUDIO
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Canvas Preview Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, CosmicBorder, RoundedCornerShape(18.dp))
                                .background(Color(0xFF070B13)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Render Artwork with generative style & filter colors
                            val baseGrad = when (selectedFilterPreset) {
                                "Cyber Glow" -> listOf(Color(0xFF0F172A), Color(0xFF06B6D4), Color(0xFF8B5CF6))
                                "Monochrome" -> listOf(Color(0xFF1E293B), Color(0xFF64748B), Color(0xFF94A3B8))
                                "Warm Film" -> listOf(Color(0xFF451A03), Color(0xFFB45309), Color(0xFFF59E0B))
                                "Vibrant Gold" -> listOf(Color(0xFF1C1917), Color(0xFFD97706), Color(0xFFFDE047))
                                "Vaporwave" -> listOf(Color(0xFF3B0764), Color(0xFFDB2777), Color(0xFF06B6D4))
                                else -> listOf(Color(0xFF0B0F19), Color(0xFF1E1B4B), Color(0xFF4C1D95), Color(0xFF06B6D4))
                            }

                            // Artwork Canvas with interactive brush doodle drawing
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(isDrawMode) {
                                        if (isDrawMode) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    currentPathPoints = listOf(offset)
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    currentPathPoints = currentPathPoints + change.position
                                                },
                                                onDragEnd = {
                                                    if (currentPathPoints.isNotEmpty()) {
                                                        paths.add(
                                                            DrawingPath(
                                                                points = currentPathPoints,
                                                                color = brushColor,
                                                                strokeWidth = brushSize
                                                            )
                                                        )
                                                        currentPathPoints = emptyList()
                                                    }
                                                }
                                            )
                                        }
                                    }
                            ) {
                                // Draw artwork background
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = baseGrad,
                                        center = Offset(size.width * 0.5f, size.height * 0.45f),
                                        radius = size.width * 0.7f
                                    )
                                )

                                // Decorative generative geometric art motifs
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.15f * contrast),
                                    radius = size.width * 0.22f,
                                    center = Offset(size.width * 0.5f, size.height * 0.45f),
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                drawCircle(
                                    color = PrimeCyan.copy(alpha = 0.25f),
                                    radius = size.width * 0.35f,
                                    center = Offset(size.width * 0.5f, size.height * 0.45f),
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                // Draw user doodle brush markup paths
                                paths.forEach { path ->
                                    if (path.points.size > 1) {
                                        for (i in 0 until path.points.size - 1) {
                                            drawLine(
                                                color = path.color,
                                                start = path.points[i],
                                                end = path.points[i + 1],
                                                strokeWidth = path.strokeWidth,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }
                                }

                                // Draw in-progress path
                                if (currentPathPoints.size > 1) {
                                    for (i in 0 until currentPathPoints.size - 1) {
                                        drawLine(
                                            color = brushColor,
                                            start = currentPathPoints[i],
                                            end = currentPathPoints[i + 1],
                                            strokeWidth = brushSize,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }

                            // Artwork Prompt Title & Caption Overlay
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                if (generatedImageTitle.isNotBlank()) {
                                    Text(
                                        text = generatedImageTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                                if (captionText.isNotBlank()) {
                                    Text(
                                        text = "“$captionText”",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimeCyan,
                                        maxLines = 2
                                    )
                                }
                                Text(
                                    text = "Prime Infinity Art • $selectedStyle • $selectedFilterPreset",
                                    fontSize = 10.sp,
                                    color = DarkTextMuted
                                )
                            }
                        }

                        // Markup & Doodle Tool Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { isDrawMode = !isDrawMode }
                                        .border(
                                            1.dp,
                                            if (isDrawMode) PrimeCyan else CosmicBorder,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    color = if (isDrawMode) PrimeCyan.copy(alpha = 0.25f) else CosmicSurfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Brush,
                                            contentDescription = null,
                                            tint = if (isDrawMode) PrimeCyan else DarkTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isDrawMode) "Doodle On" else "Doodle Markup",
                                            fontSize = 12.sp,
                                            color = if (isDrawMode) Color.White else DarkTextSecondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (paths.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { paths.clear() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Clear doodle",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Color Picker for brush
                            if (isDrawMode) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    brushColors.forEach { col ->
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(col)
                                                .border(
                                                    if (brushColor == col) 2.dp else 0.dp,
                                                    Color.White,
                                                    CircleShape
                                                )
                                                .clickable { brushColor = col }
                                        )
                                    }
                                }
                            }
                        }

                        // Filter Presets Row
                        Text(
                            text = "Color Filters:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filterPresets.forEach { preset ->
                                val isSelected = preset == selectedFilterPreset
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedFilterPreset = preset }
                                        .border(
                                            1.dp,
                                            if (isSelected) PrimeCyan else CosmicBorder,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    color = if (isSelected) PrimeCyan.copy(alpha = 0.2f) else CosmicSurface
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else DarkTextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Add Caption
                        OutlinedTextField(
                            value = captionText,
                            onValueChange = { captionText = it },
                            label = { Text("Overlay Caption Text") },
                            placeholder = { Text("Add text to stamp on image...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedFieldColors(),
                            singleLine = true
                        )

                        // Fine-tuning adjustments (Brightness & Contrast)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Contrast: ${String.format("%.1f", contrast)}", fontSize = 11.sp, color = DarkTextSecondary)
                                Slider(
                                    value = contrast,
                                    onValueChange = { contrast = it },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = PrimeCyan, activeTrackColor = PrimeCyan)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Saturation: ${String.format("%.1f", saturation)}", fontSize = 11.sp, color = DarkTextSecondary)
                                Slider(
                                    value = saturation,
                                    onValueChange = { saturation = it },
                                    valueRange = 0.0f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = PrimePink, activeTrackColor = PrimePink)
                                )
                            }
                        }

                        // Action Buttons: Send to AI Chat, Share, Export
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Check out this AI Artwork created with Prime Infinity: '${generatedImageTitle.ifBlank { promptInput }}' [Style: $selectedStyle]"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Image Artwork"))
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextSecondary)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val title = generatedImageTitle.ifBlank { promptInput.ifBlank { "AI Art Creation" } }
                                    val note = "I created an image with Prime Infinity titled '$title' in $selectedStyle style with $selectedFilterPreset filter. Please analyze this visual concept and suggest lore or next iterations."
                                    onSendImageToChat(title, note)
                                    Toast.makeText(context, "Inserted artwork into active chat!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("send_image_to_chat_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimeViolet)
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send to Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
