package com.example.ime.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardItem
import com.example.data.ClipboardRepository
import kotlinx.coroutines.launch

@Composable
fun ClipboardPanel(
    repository: ClipboardRepository,
    palette: KeyboardPalette,
    onPasteText: (String) -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyPinned by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val allClips by repository.allClips.collectAsState(initial = emptyList())
    val pinnedClips by repository.pinnedClips.collectAsState(initial = emptyList())

    val displayedClips = remember(allClips, pinnedClips, searchQuery, showOnlyPinned) {
        val baseList = if (showOnlyPinned) pinnedClips else allClips
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter { it.text.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(palette.keyboardBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Keyboard",
                        tint = palette.keyTextColor
                    )
                }

                Text(
                    text = "Clipboard (${allClips.size})",
                    color = palette.keyTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Filter Pinned Toggle
                    IconButton(
                        onClick = { showOnlyPinned = !showOnlyPinned },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (showOnlyPinned) Icons.Default.PushPin else Icons.Default.FilterList,
                            contentDescription = "Filter Pinned",
                            tint = if (showOnlyPinned) palette.actionKeyBackground else palette.stripIconColor
                        )
                    }

                    // Add Custom Clip
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Clip",
                            tint = palette.stripIconColor
                        )
                    }

                    // Clear Recent Non-Pinned
                    IconButton(
                        onClick = { coroutineScope.launch { repository.clearRecentClips() } },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Recent",
                            tint = palette.stripIconColor
                        )
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search unlimited clips...", color = palette.hintTextColor, fontSize = 12.sp)
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = palette.hintTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = palette.hintTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = palette.chipBackground,
                    unfocusedContainerColor = palette.chipBackground,
                    focusedBorderColor = palette.actionKeyBackground,
                    unfocusedBorderColor = palette.keyBorderColor,
                    focusedTextColor = palette.keyTextColor,
                    unfocusedTextColor = palette.keyTextColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Clips List
            if (displayedClips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = palette.hintTextColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No clips matching \"$searchQuery\""
                            else if (showOnlyPinned) "No pinned clips yet"
                            else "Unlimited clipboard is ready!\nAny text you copy anywhere will appear here.",
                            color = palette.hintTextColor,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(displayedClips, key = { it.id }) { item ->
                        ClipboardItemRow(
                            item = item,
                            palette = palette,
                            onPaste = { onPasteText(item.text) },
                            onTogglePin = { coroutineScope.launch { repository.togglePin(item) } },
                            onDelete = { coroutineScope.launch { repository.deleteClip(item) } }
                        )
                    }
                }
            }
        }

        // In-line Add Clip Modal Overlay (native to IME container, avoids dialog window token crash)
        if (showAddDialog) {
            var newText by remember { mutableStateOf("") }
            var isPinNew by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = palette.keyBackground,
                    border = BorderStroke(1.dp, palette.keyBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Add to Unlimited Clipboard",
                                color = palette.keyTextColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showAddDialog = false },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = palette.hintTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = newText,
                            onValueChange = { newText = it },
                            placeholder = { Text("Enter text, code, or template...", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = palette.chipBackground,
                                unfocusedContainerColor = palette.chipBackground,
                                focusedTextColor = palette.keyTextColor,
                                unfocusedTextColor = palette.keyTextColor
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { isPinNew = !isPinNew }
                            ) {
                                Checkbox(
                                    checked = isPinNew,
                                    onCheckedChange = { isPinNew = it },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pin indefinitely", color = palette.keyTextColor, fontSize = 12.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(
                                    onClick = { showAddDialog = false },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Cancel", color = palette.hintTextColor, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        if (newText.isNotBlank()) {
                                            coroutineScope.launch {
                                                repository.insertClip(newText, isPinned = isPinNew)
                                            }
                                            showAddDialog = false
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.actionKeyBackground)
                                ) {
                                    Text(
                                        "Save Clip",
                                        color = palette.actionKeyTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipboardItemRow(
    item: ClipboardItem,
    palette: KeyboardPalette,
    onPaste: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.keyBackground)
            .border(0.5.dp, palette.keyBorderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onPaste)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tag badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (item.isPinned) palette.actionKeyBackground.copy(alpha = 0.2f)
                    else palette.chipBackground
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (item.isPinned) "PINNED" else item.category.uppercase(),
                color = if (item.isPinned) palette.actionKeyBackground else palette.hintTextColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Text content preview
        Text(
            text = item.text,
            color = palette.keyTextColor,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Pin button
        IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Toggle Pin",
                tint = if (item.isPinned) palette.actionKeyBackground else palette.hintTextColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }

        // Delete button
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Clip",
                tint = palette.hintTextColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
