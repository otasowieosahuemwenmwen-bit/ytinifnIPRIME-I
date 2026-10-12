package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.PrivateBorder
import com.example.ui.theme.PrivateLockColor
import com.example.ui.theme.PrivateSurface

@Composable
fun PrivateModeBanner(
    onClearSession: () -> Unit,
    onExitPrivateMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PrivateSurface)
            .border(1.dp, PrivateBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
            .testTag("private_mode_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Private Mode Active",
                    tint = PrivateLockColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Incognito Private Mode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrivateLockColor
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Clear button
                OutlinedButton(
                    onClick = onClearSession,
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("private_clear_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", fontSize = 11.sp)
                }

                // Exit button
                OutlinedButton(
                    onClick = onExitPrivateMode,
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("private_exit_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Exit", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Conversations in this mode are strictly held in volatile memory and will never be saved to storage or shared.",
            fontSize = 11.sp,
            color = Color(0xFFA7F3D0),
            lineHeight = 15.sp
        )
    }
}
