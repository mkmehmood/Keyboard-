package com.example.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardItem

@Composable
fun KeyboardTopBar(
    palette: KeyboardPalette,
    latestClip: ClipboardItem?,
    suggestions: List<String>,
    onSuggestionClicked: (String) -> Unit,
    onPasteClip: (String) -> Unit,
    onOpenClipboard: () -> Unit,
    onOpenTextEditing: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleNumberRow: () -> Unit,
    isNumberRowVisible: Boolean,
    onVoiceInput: () -> Unit,
    onOpenThemes: () -> Unit,
    onTranslate: () -> Unit = {}
) {
    var isExpandedTools by remember { mutableStateOf(false) }

    // Gboard-style icon toolbar for the photo theme: apps, clipboard, translate, settings, mic
    if (palette.translucent && !isExpandedTools) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ToolbarIcon(Icons.Default.Apps, "Tools", palette) { isExpandedTools = true }
            ToolbarIcon(Icons.Default.ContentPaste, "Clipboard", palette, onClick = onOpenClipboard)
            ToolbarIcon(Icons.Default.Translate, "Translate", palette, onClick = onTranslate)
            ToolbarIcon(Icons.Default.Settings, "Settings", palette, onClick = onOpenSettings)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable(onClick = onVoiceInput),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        color = palette.stripBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left "G" or Quick Tools expander
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { isExpandedTools = !isExpandedTools },
                contentAlignment = Alignment.Center
            ) {
                if (isExpandedTools) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Collapse tools",
                        tint = palette.stripIconColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    // Stylized "G" Google button
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4285F4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            if (isExpandedTools) {
                // Quick Tools Row
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickToolChip(
                        icon = Icons.Default.ContentPaste,
                        label = "Clipboard",
                        palette = palette,
                        onClick = onOpenClipboard
                    )
                    QuickToolChip(
                        icon = Icons.Default.Palette,
                        label = "Theme",
                        palette = palette,
                        onClick = onOpenThemes
                    )
                    QuickToolChip(
                        icon = Icons.Default.Pin,
                        label = if (isNumberRowVisible) "123 Hide" else "123 Show",
                        palette = palette,
                        onClick = onToggleNumberRow
                    )
                    QuickToolChip(
                        icon = Icons.Default.TextFields,
                        label = "Text Edit",
                        palette = palette,
                        onClick = onOpenTextEditing
                    )
                    QuickToolChip(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        palette = palette,
                        onClick = onOpenSettings
                    )
                }
            } else {
                // Normal Suggestion / Clipboard Quick Paste Row
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick Clipboard Pill if recent clip exists
                    if (latestClip != null) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onPasteClip(latestClip.text) },
                            color = palette.actionKeyBackground.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste recent",
                                    tint = palette.actionKeyBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Paste: ${latestClip.text.take(18)}${if (latestClip.text.length > 18) "..." else ""}",
                                    color = palette.actionKeyBackground,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Word suggestions
                    suggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.chipBackground)
                                .clickable { onSuggestionClicked(suggestion) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = suggestion,
                                color = palette.chipTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // Voice / Mic button on right
                IconButton(
                    onClick = onVoiceInput,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = palette.stripIconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarIcon(
    icon: ImageVector,
    label: String,
    palette: KeyboardPalette,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = palette.stripIconColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun QuickToolChip(
    icon: ImageVector,
    label: String,
    palette: KeyboardPalette,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = palette.chipBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = palette.stripIconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = palette.keyTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
