package com.example.ime.ui

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.model.KeyAction
import com.example.ime.model.KeyType
import com.example.ime.model.KeyboardMode
import com.example.ime.model.KeyboardPresets
import com.example.ime.model.ShiftState

/**
 * Standard QWERTY keyboard composable layout featuring Material You design tokens,
 * tonal elevations, responsive keycaps, visual keypress popups, and local state management.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MaterialYouQwertyKeyboard(
    modifier: Modifier = Modifier,
    showNumberRow: Boolean = true,
    showKeyBorders: Boolean = true,
    actionLabel: String = "↵",
    onCharacterEntered: (String) -> Unit = {},
    onBackspace: () -> Unit = {},
    onSpace: () -> Unit = {},
    onEnter: () -> Unit = {},
    onModeChange: (KeyboardMode) -> Unit = {},
    onEmojiToggle: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // Local State Management for Keyboard
    var shiftState by remember { mutableStateOf(ShiftState.LOWERCASE) }
    var lastShiftTapTime by remember { mutableLongStateOf(0L) }
    var activePopupKey by remember { mutableStateOf<KeyAction?>(null) }
    var spaceDragAccumulator by remember { mutableFloatStateOf(0f) }

    val colorScheme = MaterialTheme.colorScheme
    val view = LocalView.current

    // Helper for shift key handling
    fun handleShiftPress() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShiftTapTime < 350L) {
            // Double-tap detected -> Caps Lock
            shiftState = ShiftState.CAPS_LOCKED
        } else {
            shiftState = when (shiftState) {
                ShiftState.LOWERCASE -> ShiftState.UPPERCASE
                ShiftState.UPPERCASE -> ShiftState.LOWERCASE
                ShiftState.CAPS_LOCKED -> ShiftState.LOWERCASE
            }
        }
        lastShiftTapTime = currentTime
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(colorScheme.surface)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .testTag("material_you_qwerty_keyboard")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Optional Material You Number Row
            if (showNumberRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    KeyboardPresets.numberRow.forEach { key ->
                        MaterialYouKeycap(
                            keyAction = key,
                            showBorders = showKeyBorders,
                            onClick = {
                                onCharacterEntered(key.primaryText)
                            },
                            onPressedChanged = { isPressed ->
                                activePopupKey = if (isPressed) key else null
                            }
                        )
                    }
                }
            }

            // Row 1: Q W E R T Y U I O P
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                KeyboardPresets.qwertyRow1.forEach { key ->
                    val displayChar = if (shiftState != ShiftState.LOWERCASE) {
                        key.primaryText.uppercase()
                    } else {
                        key.primaryText
                    }
                    val effectiveKey = key.copy(primaryText = displayChar, displayText = displayChar)

                    MaterialYouKeycap(
                        keyAction = effectiveKey,
                        showBorders = showKeyBorders,
                        onClick = {
                            onCharacterEntered(displayChar)
                            if (shiftState == ShiftState.UPPERCASE) {
                                shiftState = ShiftState.LOWERCASE
                            }
                        },
                        onLongClick = key.hintText?.let { hint ->
                            { onCharacterEntered(hint) }
                        },
                        onPressedChanged = { isPressed ->
                            activePopupKey = if (isPressed) effectiveKey else null
                        }
                    )
                }
            }

            // Row 2: A S D F G H J K L (centered with side indentation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                KeyboardPresets.qwertyRow2.forEach { key ->
                    val displayChar = if (shiftState != ShiftState.LOWERCASE) {
                        key.primaryText.uppercase()
                    } else {
                        key.primaryText
                    }
                    val effectiveKey = key.copy(primaryText = displayChar, displayText = displayChar)

                    MaterialYouKeycap(
                        keyAction = effectiveKey,
                        showBorders = showKeyBorders,
                        onClick = {
                            onCharacterEntered(displayChar)
                            if (shiftState == ShiftState.UPPERCASE) {
                                shiftState = ShiftState.LOWERCASE
                            }
                        },
                        onLongClick = key.hintText?.let { hint ->
                            { onCharacterEntered(hint) }
                        },
                        onPressedChanged = { isPressed ->
                            activePopupKey = if (isPressed) effectiveKey else null
                        }
                    )
                }
            }

            // Row 3: Shift, Z X C V B N M, Backspace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Shift key with visual indicator
                val shiftIcon = when (shiftState) {
                    ShiftState.LOWERCASE -> "⇧"
                    ShiftState.UPPERCASE -> "⇪"
                    ShiftState.CAPS_LOCKED -> "▲"
                }
                val isShiftActive = shiftState != ShiftState.LOWERCASE

                MaterialYouFunctionKey(
                    label = shiftIcon,
                    weight = 1.4f,
                    isSelected = shiftState == ShiftState.CAPS_LOCKED,
                    isAccent = isShiftActive,
                    onClick = { handleShiftPress() }
                )

                KeyboardPresets.qwertyRow3.forEach { key ->
                    val displayChar = if (shiftState != ShiftState.LOWERCASE) {
                        key.primaryText.uppercase()
                    } else {
                        key.primaryText
                    }
                    val effectiveKey = key.copy(primaryText = displayChar, displayText = displayChar)

                    MaterialYouKeycap(
                        keyAction = effectiveKey,
                        showBorders = showKeyBorders,
                        onClick = {
                            onCharacterEntered(displayChar)
                            if (shiftState == ShiftState.UPPERCASE) {
                                shiftState = ShiftState.LOWERCASE
                            }
                        },
                        onLongClick = key.hintText?.let { hint ->
                            { onCharacterEntered(hint) }
                        },
                        onPressedChanged = { isPressed ->
                            activePopupKey = if (isPressed) effectiveKey else null
                        }
                    )
                }

                // Backspace key
                MaterialYouFunctionKey(
                    label = "⌫",
                    weight = 1.4f,
                    onClick = onBackspace
                )
            }

            // Row 4: ?123, Comma, Emoji, Space, Period, Action/Enter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Symbols key
                MaterialYouFunctionKey(
                    label = "?123",
                    weight = 1.35f,
                    onClick = { onModeChange(KeyboardMode.SYMBOLS_1) }
                )

                // Comma key with Settings long-press
                MaterialYouKeycap(
                    keyAction = KeyAction(",", hintText = "⚙", weight = 1.0f),
                    showBorders = showKeyBorders,
                    onClick = { onCharacterEntered(",") },
                    onLongClick = onSettingsClick
                )

                // Emoji button
                MaterialYouFunctionKey(
                    label = "😀",
                    weight = 1.0f,
                    onClick = onEmojiToggle
                )

                // Material You Broad Capsule Spacebar with slide gesture
                Box(
                    modifier = Modifier
                        .weight(4.2f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colorScheme.surfaceVariant)
                        .then(
                            if (showKeyBorders) Modifier.border(
                                0.6.dp,
                                colorScheme.outlineVariant,
                                RoundedCornerShape(14.dp)
                            ) else Modifier
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    spaceDragAccumulator += dragAmount.x
                                    if (spaceDragAccumulator > 30f) {
                                        view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                                        spaceDragAccumulator = 0f
                                    } else if (spaceDragAccumulator < -30f) {
                                        view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                                        spaceDragAccumulator = 0f
                                    }
                                },
                                onDragEnd = { spaceDragAccumulator = 0f }
                            )
                        }
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onSpace()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "English (US)",
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Period key with quick question mark
                MaterialYouKeycap(
                    keyAction = KeyAction(".", hintText = "?", weight = 1.0f),
                    showBorders = showKeyBorders,
                    onClick = { onCharacterEntered(".") },
                    onLongClick = { onCharacterEntered("?") }
                )

                // Material You Dynamic Action Key (Enter / Done / Search)
                Box(
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.primary)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onEnter()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionLabel,
                        color = colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active Key Pop-up Bubble (Material You Magnifier feedback)
        activePopupKey?.let { popupKey ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.primaryContainer,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .padding(bottom = 60.dp)
                        .size(width = 54.dp, height = 58.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = popupKey.displayText,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standard Material You keycap with rounded stadium geometry and dual-label hierarchy.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.MaterialYouKeycap(
    keyAction: KeyAction,
    showBorders: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onPressedChanged: (Boolean) -> Unit = {}
) {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        onPressedChanged(isPressed)
    }

    val backgroundColor = if (isPressed) {
        colorScheme.primaryContainer
    } else {
        colorScheme.surfaceVariant
    }

    val textColor = if (isPressed) {
        colorScheme.onPrimaryContainer
    } else {
        colorScheme.onSurfaceVariant
    }

    val borderModifier = if (showBorders && !isPressed) {
        Modifier.border(0.6.dp, colorScheme.outlineVariant, RoundedCornerShape(10.dp))
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .weight(keyAction.weight)
            .fillMaxHeight()
            .then(borderModifier)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                },
                onLongClick = onLongClick?.let {
                    {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        it()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = keyAction.displayText,
            color = textColor,
            fontSize = 19.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )

        // Upper-right hint character
        if (!keyAction.hintText.isNullOrEmpty()) {
            Text(
                text = keyAction.hintText,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp)
            )
        }
    }
}

/**
 * Function keys (Shift, Backspace, Mode switches) styled with secondary Material You tokens.
 */
@Composable
private fun RowScope.MaterialYouFunctionKey(
    label: String,
    weight: Float = 1.0f,
    isSelected: Boolean = false,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme

    val bg = when {
        isSelected -> colorScheme.primary
        isAccent -> colorScheme.primaryContainer
        else -> colorScheme.secondaryContainer
    }

    val textCol = when {
        isSelected -> colorScheme.onPrimary
        isAccent -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSecondaryContainer
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = if (label.length > 3) 13.sp else 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
