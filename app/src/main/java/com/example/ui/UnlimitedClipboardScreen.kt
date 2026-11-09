package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardItem
import com.example.data.ClipboardRepository
import com.example.ui.theme.GoogleBlue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.GoogleRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UnlimitedClipboardScreen(
    repository: ClipboardRepository,
    viewModel: ClipboardViewModel = viewModel(factory = ClipboardViewModel.Factory(repository))
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedClipForDetail by remember { mutableStateOf<ClipboardItem?>(null) }

    val categories = listOf("All", "Pinned", "General", "Link", "Email", "Code", "Phone")
    val filteredClips = uiState.items
    val totalCount = uiState.totalCount
    val pinnedCount = uiState.pinnedCount
    val searchQuery = uiState.searchQuery
    val selectedCategory = uiState.selectedCategory

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoogleBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_clip_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Clip")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total Clips",
                    value = "$totalCount",
                    icon = Icons.Default.ContentPaste,
                    color = GoogleBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pinned Items",
                    value = "$pinnedCount",
                    icon = Icons.Default.PushPin,
                    color = GoogleGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Capacity",
                    value = "Unlimited",
                    icon = Icons.Default.AllInclusive,
                    color = Color(0xFFFBBC04),
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar & Clear Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search your unlimited clipboard...", fontSize = 13.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    modifier = Modifier.weight(1f).testTag("clipboard_search_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Clear non-pinned clips",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }

            // Clips List
            if (filteredClips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ContentPasteSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No clips found matching \"$searchQuery\""
                            else "No clipboard items yet in '$selectedCategory'",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Copy text anywhere on your device or tap + to add templates!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredClips, key = { it.id }) { item ->
                        ClipboardCard(
                            item = item,
                            onClick = { selectedClipForDetail = item },
                            onCopy = {
                                repository.copyToSystemClipboard(item.text, item.category)
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            onTogglePin = {
                                coroutineScope.launch { repository.togglePin(item) }
                            },
                            onDelete = {
                                coroutineScope.launch { repository.deleteClip(item) }
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedClipForDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedClipForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (item.isPinned) Icons.Default.PushPin else Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = GoogleBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clip Details", fontSize = 17.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Category: ${item.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Length: ${item.characterCount} characters", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Saved on: ${SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.copyToSystemClipboard(item.text, item.category)
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        selectedClipForDetail = null
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedClipForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Add New Clip Dialog
    if (showAddDialog) {
        var textInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("General") }
        var isPinnedInput by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Snippet / Template", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type custom clip or template...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text("Category:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("General", "Email", "Link", "Code", "Address", "Phone")) { c ->
                            FilterChip(
                                selected = categoryInput == c,
                                onClick = { categoryInput = c },
                                label = { Text(c, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isPinnedInput = !isPinnedInput }
                    ) {
                        Checkbox(checked = isPinnedInput, onCheckedChange = { isPinnedInput = it })
                        Text("Pin clip to top permanently", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            coroutineScope.launch {
                                repository.insertClip(textInput, isPinned = isPinnedInput, category = categoryInput)
                            }
                            showAddDialog = false
                            Toast.makeText(context, "Clip added!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save to Unlimited Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Recent Clips?") },
            text = {
                Text("This will remove all recent clips. Pinned clips ($pinnedCount) will remain preserved and safe.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch { repository.clearRecentClips() }
                        showClearDialog = false
                        Toast.makeText(context, "Recent clips cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleRed)
                ) {
                    Text("Clear Recent")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ClipboardCard(
    item: ClipboardItem,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (item.isPinned) GoogleBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (item.isPinned) "📌 PINNED" else item.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isPinned) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Actions row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp), tint = GoogleBlue)
                    }
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Pin",
                            modifier = Modifier.size(18.dp),
                            tint = if (item.isPinned) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${item.characterCount} chars • ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(item.timestamp))}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
