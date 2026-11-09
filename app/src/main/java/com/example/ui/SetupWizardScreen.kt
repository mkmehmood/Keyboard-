package com.example.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SetupWizardScreen(
    onNavigateToClipboard: () -> Unit
) {
    val context = LocalContext.current
    var testInputText by remember { mutableStateOf("") }
    var testNoteText by remember { mutableStateOf("") }

    // Check IME status
    fun isKeyboardEnabled(): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
        val enabledList = imm.enabledInputMethodList
        return enabledList.any { it.packageName == context.packageName }
    }

    fun isKeyboardSelected(): Boolean {
        val currentIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return currentIme != null && currentIme.contains(context.packageName)
    }

    var isEnabled by remember { mutableStateOf(isKeyboardEnabled()) }
    var isSelected by remember { mutableStateOf(isKeyboardSelected()) }

    // Recheck whenever composable recomposes
    LaunchedEffect(Unit) {
        isEnabled = isKeyboardEnabled()
        isSelected = isKeyboardSelected()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Hero Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isEnabled && isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().testTag("status_hero_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isEnabled && isSelected) GoogleGreen else GoogleYellow
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled && isSelected) Icons.Default.Check else Icons.Default.PriorityHigh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (isEnabled && isSelected) "Gboard Replica is Active!" else "Setup Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEnabled && isSelected) "Ready with unlimited copy-paste clipboard"
                        else "Complete the 2 steps below to activate system-wide",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Step 1: Enable in Settings
        Card(
            modifier = Modifier.fillMaxWidth().testTag("step_1_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepNumberBadge(number = 1, isCompleted = isEnabled)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Gboard Keyboard",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isEnabled) "Keyboard is enabled in system settings"
                            else "Turn on Gboard in Android Language & Input settings",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("enable_keyboard_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabled) MaterialTheme.colorScheme.surfaceVariant else GoogleBlue
                    )
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEnabled) "Check Settings Again" else "Open Android Settings")
                }
            }
        }

        // Step 2: Switch / Select Input Method
        Card(
            modifier = Modifier.fillMaxWidth().testTag("step_2_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepNumberBadge(number = 2, isCompleted = isSelected)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Set as Default Keyboard",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isSelected) "Gboard is currently the default input method"
                            else "Select Gboard from the Android input switcher",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showInputMethodPicker()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("select_keyboard_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else GoogleBlue
                    )
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSelected) "Switch Input Method" else "Select Gboard Keyboard")
                }
            }
        }

        // Step 3: Interactive Sandbox Test Area
        Card(
            modifier = Modifier.fillMaxWidth().testTag("sandbox_test_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = GoogleBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interactive Keyboard Sandbox",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = "Tap below to open your Gboard replica keyboard and test typing, haptics, emojis, and clipboard drawer!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = testInputText,
                    onValueChange = { testInputText = it },
                    label = { Text("Test Single Line Input") },
                    placeholder = { Text("Type here to test typing & suggestions...") },
                    modifier = Modifier.fillMaxWidth().testTag("test_input_single_line"),
                    trailingIcon = if (testInputText.isNotEmpty()) {
                        {
                            IconButton(onClick = { testInputText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    } else null
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = testNoteText,
                    onValueChange = { testNoteText = it },
                    label = { Text("Multiline Note / Copy-Paste Area") },
                    placeholder = { Text("Paste unlimited clips or try typing long notes...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("test_input_multiline")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Live Material You Keyboard Sandbox Layout
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("material_you_live_preview_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Material You QWERTY Layout",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Live Interactive",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        com.example.ime.ui.MaterialYouQwertyKeyboard(
                            showNumberRow = true,
                            showKeyBorders = true,
                            actionLabel = "↵",
                            onCharacterEntered = { char ->
                                testInputText += char
                            },
                            onBackspace = {
                                if (testInputText.isNotEmpty()) {
                                    testInputText = testInputText.dropLast(1)
                                }
                            },
                            onSpace = {
                                testInputText += " "
                            },
                            onEnter = {
                                testInputText += "\n"
                            },
                            onModeChange = {},
                            onEmojiToggle = {
                                testInputText += " 😀"
                            },
                            onSettingsClick = {}
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            testInputText = "Hello from Google Gboard replica! 🎉 🚀"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Fill Sample Text", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onNavigateToClipboard,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Clips", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepNumberBadge(number: Int, isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isCompleted) GoogleGreen else GoogleBlue),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        } else {
            Text(text = "$number", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
