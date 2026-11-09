package com.example.ime.ui

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onSettingsClicked: () -> Unit
) {
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
                    KeyboardPresets.qwertyRow1.forEach { key ->
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
                        .padding(horizontal = 14.dp)
                ) {
                    KeyboardPresets.qwertyRow2.forEach { key ->
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

                    KeyboardPresets.qwertyRow3.forEach { key ->
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
                        icon = Icons.AutoMirrored.Outlined.Backspace,
                        onClick = onBackspaceClicked
                    )
                }

                // Row 4: ?123, "/", Globe (emoji), Space, Period, Enter
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
                        onClick = { onModeChange(KeyboardMode.NUMPAD) }
                    )

                    // Long-press "/" opens keyboard settings
                    KeyButton(
                        keyAction = KeyAction("/", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = { onKeyClicked(KeyAction("/")) },
                        onLongClick = onSettingsClicked
                    )

                    KeyButton(
                        keyAction = KeyAction("🌐", weight = 1.0f, type = KeyType.EMOJI_TOGGLE),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        icon = Icons.Default.Language,
                        onClick = onEmojiClicked
                    )

                    // Spacebar with Gboard language hint
                    KeyButton(
                        keyAction = KeyAction(" ", displayText = "English", weight = 4.2f, type = KeyType.SPACE),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        textSize = 13.sp,
                        onClick = onSpaceClicked
                    )

                    KeyButton(
                        keyAction = KeyAction(".", hintText = "?", weight = 1.0f),
                        palette = palette,
                        preferences = preferences,
                        isFunctionKey = true,
                        onClick = { onKeyClicked(KeyAction(".")) },
                        onLongClick = { onKeyClicked(KeyAction("?")) }
                    )

                    // Dynamic Action / Enter Key (rounded pill like Gboard)
                    val actionLabel = getActionKeyLabel(imeAction)
                    KeyButton(
                        keyAction = KeyAction("enter", displayText = actionLabel, weight = 1.4f, type = KeyType.ENTER),
                        palette = palette,
                        preferences = preferences,
                        isActionKey = true,
                        icon = getActionKeyIcon(imeAction),
                        cornerRadius = 24.dp,
                        onClick = onEnterClicked
                    )
                }
            }

            KeyboardMode.NUMPAD -> {
                NumpadLayout(
                    imeAction = imeAction,
                    preferences = preferences,
                    palette = palette,
                    onKeyClicked = onKeyClicked,
                    onBackspaceClicked = onBackspaceClicked,
                    onSpaceClicked = onSpaceClicked,
                    onEnterClicked = onEnterClicked,
                    onModeChange = onModeChange
                )
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

private fun getActionKeyIcon(imeAction: Int): androidx.compose.ui.graphics.vector.ImageVector? {
    return when (imeAction and EditorInfo.IME_MASK_ACTION) {
        EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_DONE -> Icons.AutoMirrored.Filled.ArrowForward
        else -> null
    }
}

/**
 * Number & operator pad (Gboard style): operator column on the left, 3x3 digits,
 * "%" / space / backspace on the right, and a bottom row with ABC, ",", "!?#", 0, "=", ".", Enter.
 */
@Composable
private fun NumpadLayout(
    imeAction: Int,
    preferences: KeyboardPreferences,
    palette: KeyboardPalette,
    onKeyClicked: (KeyAction) -> Unit,
    onBackspaceClicked: () -> Unit,
    onSpaceClicked: () -> Unit,
    onEnterClicked: () -> Unit,
    onModeChange: (KeyboardMode) -> Unit
) {
    val digitRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    val operators = listOf("+", "-", "*", "/")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(3 * 52.dp + 2 * 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Operator column (plain text, no key background)
        Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
            operators.forEach { op ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { onKeyClicked(KeyAction(op)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = op,
                        color = palette.keyTextColor.copy(alpha = 0.85f),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Digits 3x3
        Column(
            modifier = Modifier.weight(3f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            digitRows.forEach { row ->
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    row.forEach { d ->
                        KeyButton(
                            keyAction = KeyAction(d),
                            palette = palette,
                            preferences = preferences,
                            textSize = 28.sp,
                            cornerRadius = 12.dp,
                            onClick = { onKeyClicked(KeyAction(d)) }
                        )
                    }
                }
            }
        }

        // Right column: %, space, backspace
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                KeyButton(
                    keyAction = KeyAction("%"),
                    palette = palette,
                    preferences = preferences,
                    isFunctionKey = true,
                    textSize = 22.sp,
                    cornerRadius = 12.dp,
                    onClick = { onKeyClicked(KeyAction("%")) }
                )
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                KeyButton(
                    keyAction = KeyAction(" ", displayText = "Space"),
                    palette = palette,
                    preferences = preferences,
                    isFunctionKey = true,
                    icon = Icons.Default.SpaceBar,
                    cornerRadius = 12.dp,
                    onClick = onSpaceClicked
                )
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                KeyButton(
                    keyAction = KeyAction("backspace", displayText = "⌫", type = KeyType.BACKSPACE),
                    palette = palette,
                    preferences = preferences,
                    isFunctionKey = true,
                    icon = Icons.AutoMirrored.Outlined.Backspace,
                    cornerRadius = 12.dp,
                    onClick = onBackspaceClicked
                )
            }
        }
    }

    // Bottom row: ABC , !?# 0 = . Enter
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        KeyButton(
            keyAction = KeyAction("ABC", weight = 1.2f),
            palette = palette,
            preferences = preferences,
            isFunctionKey = true,
            onClick = { onModeChange(KeyboardMode.QWERTY) }
        )
        KeyButton(
            keyAction = KeyAction(",", weight = 0.9f),
            palette = palette,
            preferences = preferences,
            isFunctionKey = true,
            onClick = { onKeyClicked(KeyAction(",")) }
        )
        KeyButton(
            keyAction = KeyAction("!?#", weight = 1.2f),
            palette = palette,
            preferences = preferences,
            isFunctionKey = true,
            onClick = { onModeChange(KeyboardMode.SYMBOLS_1) }
        )
        KeyButton(
            keyAction = KeyAction("0", weight = 1.6f),
            palette = palette,
            preferences = preferences,
            textSize = 28.sp,
            cornerRadius = 12.dp,
            onClick = { onKeyClicked(KeyAction("0")) }
        )
        KeyButton(
            keyAction = KeyAction("=", weight = 1.0f),
            palette = palette,
            preferences = preferences,
            isFunctionKey = true,
            onClick = { onKeyClicked(KeyAction("=")) }
        )
        KeyButton(
            keyAction = KeyAction(".", weight = 0.8f),
            palette = palette,
            preferences = preferences,
            isFunctionKey = true,
            onClick = { onKeyClicked(KeyAction(".")) }
        )
        val actionLabel = getActionKeyLabel(imeAction)
        KeyButton(
            keyAction = KeyAction("enter", displayText = actionLabel, weight = 1.5f, type = KeyType.ENTER),
            palette = palette,
            preferences = preferences,
            isActionKey = true,
            icon = getActionKeyIcon(imeAction) ?: Icons.AutoMirrored.Filled.ArrowForward,
            cornerRadius = 26.dp,
            onClick = onEnterClicked
        )
    }
}
