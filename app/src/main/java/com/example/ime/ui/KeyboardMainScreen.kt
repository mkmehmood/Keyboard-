package com.example.ime.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardRepository
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardTheme
import com.example.ime.GboardImeService
import com.example.ime.model.KeyAction
import com.example.ime.model.KeyboardMode
import com.example.ime.model.ShiftState

@Composable
fun KeyboardMainScreen(
    service: GboardImeService,
    repository: ClipboardRepository,
    preferences: KeyboardPreferences
) {
    var mode by remember { mutableStateOf<KeyboardMode>(KeyboardMode.QWERTY) }
    var shiftState by remember { mutableStateOf(ShiftState.LOWERCASE) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Observe latest clip from clipboard repository
    val allClips by repository.allClips.collectAsState(initial = emptyList())
    val latestClip = allClips.firstOrNull()

    // Active palette based on preferences
    val currentTheme = preferences.theme
    val palette = remember(currentTheme) {
        KeyboardThemePalettes.getPalette(currentTheme)
    }

    // Optional custom photo background (only used by the "My Photo" theme)
    val backgroundBitmap = remember(palette.translucent, preferences.backgroundImagePath) {
        if (palette.translucent) {
            preferences.backgroundImagePath?.let { path ->
                runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
            }
        } else {
            null
        }
    }

    // Number row state
    var isNumberRowVisible by remember { mutableStateOf(preferences.showNumberRow) }

    // Dynamic suggestions
    val commonSuggestions = remember {
        listOf("the", "I", "to", "and", "a", "in", "is", "you", "that", "it", "he", "was", "for", "on", "are", "as", "with")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(palette.keyboardBackground)
    ) {
        if (backgroundBitmap != null) {
            Image(
                bitmap = backgroundBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            // Dark scrim keeps white key labels readable on any photo
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Top Strip (Suggestions / Quick Action Icons)
            if (preferences.showSuggestions && mode !in listOf(KeyboardMode.CLIPBOARD, KeyboardMode.EMOJI, KeyboardMode.TEXT_EDIT)) {
                KeyboardTopBar(
                    palette = palette,
                    latestClip = latestClip,
                    suggestions = commonSuggestions.take(3),
                    onSuggestionClicked = { suggestion ->
                        service.currentInputConnection?.commitText("$suggestion ", 1)
                    },
                    onPasteClip = { clipText ->
                        service.currentInputConnection?.commitText(clipText, 1)
                    },
                    onOpenClipboard = { mode = KeyboardMode.CLIPBOARD },
                    onOpenTextEditing = { mode = KeyboardMode.TEXT_EDIT },
                    onOpenSettings = {
                        val intent = Intent(service, com.example.MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        service.startActivity(intent)
                    },
                    onToggleNumberRow = {
                        isNumberRowVisible = !isNumberRowVisible
                        preferences.showNumberRow = isNumberRowVisible
                    },
                    isNumberRowVisible = isNumberRowVisible,
                    onVoiceInput = {
                        service.startVoiceRecognition()
                    },
                    onOpenThemes = { showThemeDialog = true },
                    onTranslate = {
                        val launch = service.packageManager
                            .getLaunchIntentForPackage("com.google.android.apps.translate")
                        if (launch != null) {
                            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            service.startActivity(launch)
                        }
                    }
                )
            }

            // Active Panel
            when (mode) {
                KeyboardMode.CLIPBOARD -> {
                    ClipboardPanel(
                        repository = repository,
                        palette = palette,
                        onPasteText = { text ->
                            service.currentInputConnection?.commitText(text, 1)
                        },
                        onClose = { mode = KeyboardMode.QWERTY }
                    )
                }

                KeyboardMode.EMOJI -> {
                    EmojiPanel(
                        palette = palette,
                        onEmojiSelected = { emoji ->
                            service.currentInputConnection?.commitText(emoji, 1)
                        },
                        onBackspace = {
                            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                        },
                        onClose = { mode = KeyboardMode.QWERTY }
                    )
                }

                KeyboardMode.TEXT_EDIT -> {
                    TextEditingPanel(
                        palette = palette,
                        onMoveLeft = {
                            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
                        },
                        onMoveRight = {
                            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
                        },
                        onMoveUp = {
                            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP)
                        },
                        onMoveDown = {
                            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN)
                        },
                        onSelectAll = {
                            service.currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                        },
                        onCopy = {
                            service.currentInputConnection?.performContextMenuAction(android.R.id.copy)
                            repository.syncSystemClipboard()
                        },
                        onCut = {
                            service.currentInputConnection?.performContextMenuAction(android.R.id.cut)
                            repository.syncSystemClipboard()
                        },
                        onPaste = {
                            service.currentInputConnection?.performContextMenuAction(android.R.id.paste)
                        },
                        onClose = { mode = KeyboardMode.QWERTY }
                    )
                }

                KeyboardMode.QWERTY, KeyboardMode.NUMPAD, KeyboardMode.SYMBOLS_1, KeyboardMode.SYMBOLS_2 -> {
                  // One-handed mode squeezes the keys to one side of the screen
                  val oneHanded = preferences.oneHandedMode
                  Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (oneHanded == "left") Alignment.TopStart else Alignment.TopEnd
                  ) {
                   Box(modifier = if (oneHanded == "off") Modifier.fillMaxWidth() else Modifier.fillMaxWidth(0.82f)) {
                    KeyboardLayoutView(
                        heightScale = preferences.keyboardHeightScale,
                        mode = mode,
                        shiftState = shiftState,
                        imeAction = service.currentInputEditorInfo?.imeOptions ?: EditorInfo.IME_ACTION_DONE,
                        preferences = preferences,
                        palette = palette,
                        onKeyClicked = { action ->
                            service.currentInputConnection?.commitText(action.primaryText, 1)
                            if (shiftState == ShiftState.UPPERCASE) {
                                shiftState = ShiftState.LOWERCASE
                            }
                        },
                        onShiftClicked = {
                            shiftState = when (shiftState) {
                                ShiftState.LOWERCASE -> ShiftState.UPPERCASE
                                ShiftState.UPPERCASE -> ShiftState.LOWERCASE
                                ShiftState.CAPS_LOCKED -> ShiftState.LOWERCASE
                            }
                        },
                        onShiftDoubleClicked = {
                            shiftState = ShiftState.CAPS_LOCKED
                        },
                        onBackspaceClicked = {
                            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                        },
                        onSpaceClicked = {
                            service.currentInputConnection?.commitText(" ", 1)
                        },
                        onEnterClicked = {
                            val editorInfo = service.currentInputEditorInfo
                            val action = editorInfo?.imeOptions ?: EditorInfo.IME_ACTION_NONE
                            if ((action and EditorInfo.IME_MASK_ACTION) != EditorInfo.IME_ACTION_NONE) {
                                service.currentInputConnection?.performEditorAction(action and EditorInfo.IME_MASK_ACTION)
                            } else {
                                service.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                            }
                        },
                        onModeChange = { newMode -> mode = newMode },
                        onEmojiClicked = { mode = KeyboardMode.EMOJI },
                        onSettingsClicked = {
                            val intent = Intent(service, com.example.MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            service.startActivity(intent)
                        }
                    )
                   }
                  }
                }
            }

            // Bottom margin (clears the gesture bar / rounded screen corners)
            Spacer(modifier = Modifier.height(preferences.keyboardBottomPaddingDp.dp))
        }

        // Live Theme Selector In-Keyboard Overlay (replaces platform AlertDialog to prevent window token crash)
        if (showThemeDialog) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showThemeDialog = false
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {},
                    shape = RoundedCornerShape(16.dp),
                    color = palette.keyBackground,
                    tonalElevation = 6.dp
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
                                text = "Choose Gboard Theme",
                                color = palette.keyTextColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showThemeDialog = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = palette.hintTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            items(KeyboardTheme.values()) { t ->
                                val isSelected = preferences.theme == t
                                val themePalette = KeyboardThemePalettes.getPalette(t)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(themePalette.keyboardBackground)
                                        .border(
                                            if (isSelected) 2.dp else 0.5.dp,
                                            if (isSelected) palette.actionKeyBackground else palette.keyBorderColor,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            preferences.theme = t
                                            showThemeDialog = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = t.title,
                                        color = themePalette.keyTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
