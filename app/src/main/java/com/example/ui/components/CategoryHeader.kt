package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryItem
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.PrivateBorder
import com.example.ui.theme.PrivateLockColor
import com.example.ui.theme.PrivateSurface
import com.example.ui.theme.PrimeCyan
import com.example.ui.theme.PrimeViolet

@Composable
fun CategoryHeader(
    categories: List<CategoryItem>,
    activeCategory: CategoryItem,
    isPrivateMode: Boolean,
    onCategorySelected: (CategoryItem) -> Unit,
    onTogglePrivateMode: (Boolean) -> Unit,
    onOpenCategoryManager: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurface)
            .border(width = 0.5.dp, color = CosmicBorder)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dedicated Private AI Chat Tab
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onTogglePrivateMode(!isPrivateMode) }
                    .border(
                        width = if (isPrivateMode) 1.5.dp else 1.dp,
                        color = if (isPrivateMode) PrivateLockColor else PrivateBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("private_ai_chat_tab"),
                color = if (isPrivateMode) PrivateLockColor.copy(alpha = 0.25f) else PrivateSurface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = PrivateLockColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Private AI Chat",
                        fontSize = 13.sp,
                        fontWeight = if (isPrivateMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (isPrivateMode) Color.White else PrivateLockColor
                    )
                    if (isPrivateMode) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(PrivateLockColor)
                        )
                    }
                }
            }

            // Categories
            categories.forEach { category ->
                val isSelected = !isPrivateMode && category.id == activeCategory.id
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(category.colorHex))
                } catch (e: Exception) {
                    PrimeViolet
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            if (isPrivateMode) {
                                onTogglePrivateMode(false)
                            }
                            onCategorySelected(category)
                        }
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) parsedColor else CosmicBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .testTag("category_tab_${category.id}"),
                    color = if (isSelected) parsedColor.copy(alpha = 0.22f) else CosmicSurfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.iconEmoji,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else DarkTextSecondary
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor)
                            )
                        }
                    }
                }
            }

            // Manage / Add Category Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenCategoryManager() }
                    .border(1.dp, PrimeCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .testTag("manage_categories_button"),
                color = CosmicSurfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom category",
                        tint = PrimeCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Customize",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimeCyan
                    )
                }
            }
        }

        // Active Category Subtitle & Tone Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val parsedColor = if (isPrivateMode) {
                PrivateLockColor
            } else {
                try {
                    Color(android.graphics.Color.parseColor(activeCategory.colorHex))
                } catch (e: Exception) {
                    PrimeViolet
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isPrivateMode) "Security: " else "Mode: ",
                    fontSize = 11.sp,
                    color = DarkTextMuted
                )
                Text(
                    text = if (isPrivateMode) "Zero-Log Ephemeral Encryption" else activeCategory.toneLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = parsedColor
                )
            }

            Text(
                text = if (isPrivateMode) "🔒 Messages auto-purge on exit" else activeCategory.subtitle,
                fontSize = 11.sp,
                color = DarkTextSecondary,
                maxLines = 1
            )
        }
    }
}
