package com.example.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextEditingPanel(
    palette: KeyboardPalette,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(palette.keyboardBackground)
            .padding(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = palette.keyTextColor
                )
            }
            Text(
                text = "Text Editing",
                color = palette.keyTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(36.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            // Action buttons column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButtonItem("Select All", Icons.Default.SelectAll, palette, onSelectAll)
                ActionButtonItem("Copy", Icons.Default.ContentCopy, palette, onCopy)
                ActionButtonItem("Cut", Icons.Default.ContentCut, palette, onCut)
                ActionButtonItem("Paste", Icons.Default.ContentPaste, palette, onPaste)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Navigation D-Pad
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Up arrow
                DPadButton(Icons.Default.KeyboardArrowUp, "Up", palette, onMoveUp)

                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DPadButton(Icons.Default.KeyboardArrowLeft, "Left", palette, onMoveLeft)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("•", color = palette.hintTextColor, fontSize = 20.sp)
                    }
                    DPadButton(Icons.Default.KeyboardArrowRight, "Right", palette, onMoveRight)
                }

                // Down arrow
                DPadButton(Icons.Default.KeyboardArrowDown, "Down", palette, onMoveDown)
            }
        }
    }
}

@Composable
private fun ColumnScope.ActionButtonItem(
    label: String,
    icon: ImageVector,
    palette: KeyboardPalette,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.keyBackground)
            .border(0.5.dp, palette.keyBorderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = palette.actionKeyBackground, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = palette.keyTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DPadButton(
    icon: ImageVector,
    description: String,
    palette: KeyboardPalette,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.keyBackground)
            .border(0.5.dp, palette.keyBorderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = palette.keyTextColor, modifier = Modifier.size(28.dp))
    }
}
