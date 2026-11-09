package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardTheme
import com.example.ime.ui.KeyboardThemePalettes
import com.example.ui.theme.GoogleBlue

@Composable
fun KeyboardSettingsScreen(
    preferences: KeyboardPreferences
) {
    var selectedTheme by remember { mutableStateOf(preferences.theme) }
    var keyBorders by remember { mutableStateOf(preferences.showKeyBorders) }
    var numberRow by remember { mutableStateOf(preferences.showNumberRow) }
    var vibrate by remember { mutableStateOf(preferences.vibrateOnKeypress) }
    var vibrateIntensity by remember { mutableFloatStateOf(preferences.vibrateIntensity) }
    var sound by remember { mutableStateOf(preferences.soundOnKeypress) }
    var autoCap by remember { mutableStateOf(preferences.autoCapitalize) }
    var suggestions by remember { mutableStateOf(preferences.showSuggestions) }
    var autoCaptureClip by remember { mutableStateOf(preferences.autoCaptureClipboard) }
    var keyPopup by remember { mutableStateOf(preferences.keyPopupEnabled) }
    var longPressDelay by remember { mutableFloatStateOf(preferences.longPressDelayMs.toFloat()) }
    var photoSet by remember { mutableStateOf(preferences.backgroundImagePath != null) }

    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val saved = saveKeyboardBackground(context, uri, preferences.backgroundImagePath)
            if (saved != null) {
                preferences.backgroundImagePath = saved
                preferences.theme = KeyboardTheme.PHOTO
                selectedTheme = KeyboardTheme.PHOTO
                photoSet = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Selector Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = GoogleBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keyboard Themes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(KeyboardTheme.values()) { theme ->
                        val isSelected = theme == selectedTheme
                        val palette = KeyboardThemePalettes.getPalette(theme)

                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .height(100.dp)
                                .clickable {
                                    selectedTheme = theme
                                    preferences.theme = theme
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.5.dp, GoogleBlue) else null,
                            colors = CardDefaults.cardColors(containerColor = palette.keyboardBackground)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Key preview
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(palette.keyBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Q", color = palette.keyTextColor, fontSize = 9.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(palette.actionKeyBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("↵", color = palette.actionKeyTextColor, fontSize = 9.sp)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = theme.title,
                                        color = palette.keyTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = GoogleBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom photo background
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { photoPicker.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (photoSet) "Change background photo" else "Choose background photo")
                    }
                    if (photoSet) {
                        TextButton(onClick = {
                            preferences.backgroundImagePath?.let { java.io.File(it).delete() }
                            preferences.backgroundImagePath = null
                            photoSet = false
                        }) { Text("Remove") }
                    }
                }
            }
        }

        // Layout & Behavior Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Layout & Keys", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                SettingToggleRow(
                    title = "Key Borders",
                    subtitle = "Show outlines around individual keycaps",
                    icon = Icons.Default.CropFree,
                    checked = keyBorders,
                    onCheckedChange = {
                        keyBorders = it
                        preferences.showKeyBorders = it
                    }
                )

                SettingToggleRow(
                    title = "Number Row",
                    subtitle = "Always show dedicated number row (1-0) at top",
                    icon = Icons.Default.Pin,
                    checked = numberRow,
                    onCheckedChange = {
                        numberRow = it
                        preferences.showNumberRow = it
                    }
                )

                SettingToggleRow(
                    title = "Key Pop-up",
                    subtitle = "Show enlarged popup when keys are pressed",
                    icon = Icons.Default.VerticalAlignTop,
                    checked = keyPopup,
                    onCheckedChange = {
                        keyPopup = it
                        preferences.keyPopupEnabled = it
                    }
                )

                SettingToggleRow(
                    title = "Auto-Capitalization",
                    subtitle = "Capitalize first word of each sentence",
                    icon = Icons.Default.FormatSize,
                    checked = autoCap,
                    onCheckedChange = {
                        autoCap = it
                        preferences.autoCapitalize = it
                    }
                )

                SettingToggleRow(
                    title = "Suggestions & Quick Bar",
                    subtitle = "Display predictive words and clipboard quick tools",
                    icon = Icons.Default.ViewAgenda,
                    checked = suggestions,
                    onCheckedChange = {
                        suggestions = it
                        preferences.showSuggestions = it
                    }
                )
            }
        }

        // Unlimited Copy-Paste Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AllInclusive, contentDescription = null, tint = GoogleBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlimited Copy-Paste Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                SettingToggleRow(
                    title = "Auto-Capture System Clipboard",
                    subtitle = "Automatically archive every copied text into your unlimited database",
                    icon = Icons.Default.ContentPasteGo,
                    checked = autoCaptureClip,
                    onCheckedChange = {
                        autoCaptureClip = it
                        preferences.autoCaptureClipboard = it
                    }
                )
            }
        }

        // Haptics & Sound Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Haptics & Audio", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                SettingToggleRow(
                    title = "Haptic Vibration",
                    subtitle = "Vibrate on keypress for tactile typing feedback",
                    icon = Icons.Default.Vibration,
                    checked = vibrate,
                    onCheckedChange = {
                        vibrate = it
                        preferences.vibrateOnKeypress = it
                    }
                )

                SettingToggleRow(
                    title = "Sound on Keypress",
                    subtitle = "Play authentic click audio on every tap",
                    icon = Icons.Default.VolumeUp,
                    checked = sound,
                    onCheckedChange = {
                        sound = it
                        preferences.soundOnKeypress = it
                    }
                )

                // Long Press Delay Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Long Press Delay", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("${longPressDelay.toInt()} ms", fontSize = 13.sp, color = GoogleBlue, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = longPressDelay,
                        onValueChange = {
                            longPressDelay = it
                            preferences.longPressDelayMs = it.toLong()
                        },
                        valueRange = 150f..600f,
                        steps = 8
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * Copies the picked photo into app storage, scaled down (max 1080 px on the long side) so the
 * keyboard can decode it quickly. Returns the new file path, or null on failure.
 */
private fun saveKeyboardBackground(context: android.content.Context, uri: Uri, oldPath: String?): String? {
    return try {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2160) sample *= 2
        val decode = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decode) }
            ?: return null

        val longSide = maxOf(decoded.width, decoded.height)
        val bitmap = if (longSide > 1080) {
            val scale = 1080f / longSide
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).toInt().coerceAtLeast(1),
                (decoded.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else decoded

        // Unique name so the keyboard notices a changed photo
        val out = java.io.File(context.filesDir, "keyboard_bg_${System.currentTimeMillis()}.jpg")
        out.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        oldPath?.let { java.io.File(it).delete() }
        out.absolutePath
    } catch (e: Exception) {
        null
    }
}
