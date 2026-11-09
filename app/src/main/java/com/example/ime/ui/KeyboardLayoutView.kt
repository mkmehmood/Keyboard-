package com.example.ime.ui

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.KeyboardPreferences
import com.example.ime.model.*

@Composable
fun KeyboardLayoutView(
    mode: KeyboardMode,
    shiftState: ShiftState,
    imeAction: Int,
    preferences: KeyboardPreferences,
    palette: KeyboardPalette,
    onKeyClicked: (KeyAction) -> Unit,
    onShiftClicked: () -> Unit,
    onShiftDoubleClicked: () -> Unit,
    onBackspaceClicked: () -> Unit,
    onSpaceClicked: () -> Unit,
    onEnterClicked: () -> Unit,
    onModeChange: (KeyboardMode) -> Unit,
    onEmojiClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    layout: LayoutKind = LayoutKind.QWERTY
) {
    val letterRows = KeyboardPresets.rowsFor(layout)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (mode) {
            KeyboardMode.QWERTY -> {
                // Optional Dedicated Number Row
                if (preferences.showNumberRow) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        KeyboardPresets.numberRow.forEach { key ->
                            KeyButton(
                                keyAction = key,
                                palette = palette,
                                preferences = preferences,
                                onClick = { onKeyClicked(key) }
                            )
                        }
                    }
                }

                // Row 1: Q W E R T Y U I O P
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    letterRows[0].forEach { key ->
                        val char = if (shiftState != ShiftState.LOWERCASE) key.primaryText.uppercase() else key.primaryText
                        KeyButton(
                            keyAction = key.copy(displayText = char),
                            palette = palette,
                            preferences = preferences,
                            onClick = { onKeyClicked(key.copy(primaryText = char)) },
                            onLongClick = key.hintText?.let { hint -> { onKeyClicked(KeyAction(hint)) } }
                        )
                    }
                }

                // Row 2: A S D F G H J K L (indented)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = if (letterRows[1].size >= 10) 0.dp else 14.dp)
                ) {
                    letterRows[1].forEach { key ->
                        val char = if (shiftState != ShiftState.LOWERCASE) key.primaryText.uppercase() else key.primaryText
                        KeyButton(
                            keyAction = key.copy(displayText = char),
                            palette = palette,
                            preferences = preferences,
                            onClick = { onKeyClicked(key.copy(primaryText = char)) },
                            onLongClick = key.hintText?.let { hint -> { onKeyClicked(KeyAction(hint)) } }
                        )
                    }
                }

                // Row 3: Shift, Z X C V B N M, Backspace
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    // Shift Key
                    val shiftIcon = when (shiftState) {
                        ShiftState.LOWERCASE -> "⇧"
                        ShiftState.UPPERCASE -> "⇪"
                        ShiftState.CAPS_LOCKED -> "▲"
                    }
                    KeyButton(
                        keyAction = KeyAction("shift", displayText = shiftIcon, weight = 1.35f, type = KeyType.SHIFT),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        isSelected = shiftState == ShiftState.CAPS_LOCKED,
                        onClick = onShiftClicked,
                        onLongClick = onShiftDoubleClicked
                    )

                    letterRows[2].forEach { key ->
                        val char = if (shiftState != ShiftState.LOWERCASE) key.primaryText.uppercase() else key.primaryText
                        KeyButton(
                            keyAction = key.copy(displayText = char),
                            palette = palette,
                            preferences = preferences,
                            onClick = { onKeyClicked(key.copy(primaryText = char)) },
                            onLongClick = key.hintText?.let { hint -> { onKeyClicked(KeyAction(hint)) } }
                        )
                    }

                    // Backspace Key
                    KeyButton(
                        keyAction = KeyAction("backspace", displayText = "⌫", weight = 1.35f, type = KeyType.BACKSPACE),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = onBackspaceClicked
                    )
                }

                // Row 4: ?123, Comma, Emoji, Space, Period, Enter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    KeyButton(
                        keyAction = KeyAction("?123", weight = 1.4f, type = KeyType.MODE_CHANGE),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = { onModeChange(KeyboardMode.SYMBOLS_1) }
                    )

                    KeyButton(
                        keyAction = KeyAction(",", hintText = "⚙", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        onClick = { onKeyClicked(KeyAction(",")) },
                        onLongClick = onSettingsClicked
                    )

                    KeyButton(
                        keyAction = KeyAction("😀", weight = 1.0f, type = KeyType.EMOJI_TOGGLE),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = onEmojiClicked
                    )

                    // Spacebar with Gboard language hint
                    KeyButton(
                        keyAction = KeyAction(" ", displayText = "English (US)", weight = 4.2f, type = KeyType.SPACE),
                        palette = palette,
                        preferences = preferences,
                        onClick = onSpaceClicked
                    )

                    KeyButton(
                        keyAction = KeyAction(".", hintText = "?", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        onClick = { onKeyClicked(KeyAction(".")) },
                        onLongClick = { onKeyClicked(KeyAction("?")) }
                    )

                    // Dynamic Action / Enter Key
                    val actionLabel = getActionKeyLabel(imeAction)
                    KeyButton(
                        keyAction = KeyAction("enter", displayText = actionLabel, weight = 1.4f, type = KeyType.ENTER),
                        palette = palette,
                        preferences = preferences,
                        isActionKey = true,
                        onClick = onEnterClicked
                    )
                }
            }

            KeyboardMode.SYMBOLS_1 -> {
                // Symbols 1 layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    KeyboardPresets.numberRow.forEach { key ->
                        KeyButton(keyAction = key, palette = palette, preferences = preferences, onClick = { onKeyClicked(key) })
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    KeyboardPresets.symbolsRow1.forEach { key ->
                        KeyButton(keyAction = key, palette = palette, preferences = preferences, onClick = { onKeyClicked(key) })
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 14.dp)
                ) {
                    KeyboardPresets.symbolsRow2.forEach { key ->
                        KeyButton(keyAction = key, palette = palette, preferences = preferences, onClick = { onKeyClicked(key) })
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    KeyButton(
                        keyAction = KeyAction("=\\<", displayText = "=\\<", weight = 1.35f),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = { onModeChange(KeyboardMode.SYMBOLS_2) }
                    )

                    KeyboardPresets.symbolsRow3.forEach { key ->
                        KeyButton(keyAction = key, palette = palette, preferences = preferences, onClick = { onKeyClicked(key) })
                    }

                    KeyButton(
                        keyAction = KeyAction("backspace", displayText = "⌫", weight = 1.35f, type = KeyType.BACKSPACE),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = onBackspaceClicked
                    )
                }

                // Bottom row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    KeyButton(
                        keyAction = KeyAction("ABC", weight = 1.4f),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = { onModeChange(KeyboardMode.QWERTY) }
                    )

                    KeyButton(
                        keyAction = KeyAction("/", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        onClick = { onKeyClicked(KeyAction("/")) }
                    )

                    KeyButton(
                        keyAction = KeyAction("😀", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = onEmojiClicked
                    )

                    KeyButton(
                        keyAction = KeyAction(" ", displayText = "Space", weight = 4.2f),
                        palette = palette,
                        preferences = preferences,
                        onClick = onSpaceClicked
                    )

                    KeyButton(
                        keyAction = KeyAction(".", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        onClick = { onKeyClicked(KeyAction(".")) }
                    )

                    val actionLabel = getActionKeyLabel(imeAction)
                    KeyButton(
                        keyAction = KeyAction("enter", displayText = actionLabel, weight = 1.4f),
                        palette = palette,
                        preferences = preferences,
                        isActionKey = true,
                        onClick = onEnterClicked
                    )
                }
            }

            KeyboardMode.SYMBOLS_2 -> {
                // Symbols 2 layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    KeyboardPresets.symbolsPage2Row1.forEach { key ->
                        KeyButton(keyAction = key, palette = palette, preferences = preferences, onClick = { onKeyClicked(key) })
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 14.dp)
                ) {
                    KeyboardPresets.symbolsPage2Row2.forEach { key ->
                        KeyButton(keyAction = key, palette = palette, preferences = preferences, onClick = { onKeyClicked(key) })
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    KeyButton(
                        keyAction = KeyAction("?123", displayText = "?123", weight = 1.35f),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = { onModeChange(KeyboardMode.SYMBOLS_1) }
                    )

                    KeyboardPresets.symbolsPage2Row3.forEach { key ->
                        KeyButton(keyAction = key, palette = palette, preferences = preferences, onClick = { onKeyClicked(key) })
                    }

                    KeyButton(
                        keyAction = KeyAction("backspace", displayText = "⌫", weight = 1.35f, type = KeyType.BACKSPACE),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = onBackspaceClicked
                    )
                }

                // Bottom row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    KeyButton(
                        keyAction = KeyAction("ABC", weight = 1.4f),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = { onModeChange(KeyboardMode.QWERTY) }
                    )

                    KeyButton(
                        keyAction = KeyAction("\\", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        onClick = { onKeyClicked(KeyAction("\\")) }
                    )

                    KeyButton(
                        keyAction = KeyAction("😀", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = onEmojiClicked
                    )

                    KeyButton(
                        keyAction = KeyAction(" ", displayText = "Space", weight = 4.2f),
                        palette = palette,
                        preferences = preferences,
                        onClick = onSpaceClicked
                    )

                    KeyButton(
                        keyAction = KeyAction(".", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        onClick = { onKeyClicked(KeyAction(".")) }
                    )

                    val actionLabel = getActionKeyLabel(imeAction)
                    KeyButton(
                        keyAction = KeyAction("enter", displayText = actionLabel, weight = 1.4f),
                        palette = palette,
                        preferences = preferences,
                        isActionKey = true,
                        onClick = onEnterClicked
                    )
                }
            }

            else -> {
                // Handled in parent screen for EMOJI, CLIPBOARD, TEXT_EDIT
            }
        }
    }
}

private fun getActionKeyLabel(imeAction: Int): String {
    return when (imeAction and EditorInfo.IME_MASK_ACTION) {
        EditorInfo.IME_ACTION_SEARCH -> "🔍"
        EditorInfo.IME_ACTION_GO -> "Go"
        EditorInfo.IME_ACTION_SEND -> "➤"
        EditorInfo.IME_ACTION_NEXT -> "Next"
        EditorInfo.IME_ACTION_DONE -> "✓"
        else -> "↵"
    }
}
