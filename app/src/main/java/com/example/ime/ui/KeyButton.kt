package com.example.ime.ui

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences
import com.example.ime.model.KeyAction
import com.example.ime.model.KeyType
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.KeyButton(
    keyAction: KeyAction,
    palette: KeyboardPalette,
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier,
    isFunctionKey: Boolean = false,
    isActionKey: Boolean = false,
    isSelected: Boolean = false,
    icon: ImageVector? = null,
    textSize: TextUnit = TextUnit.Unspecified,
    cornerRadius: Dp = 8.dp,
    /** Repeat [onClick] while the key is held (Gboard backspace). */
    holdRepeat: Boolean = false,
    /** Horizontal distance (dp) the finger must travel for each [onHorizontalDrag] step. */
    dragStepDp: Dp = 18.dp,
    /** Called with a signed step count while dragging sideways (negative = left). */
    onHorizontalDrag: ((Int) -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val normalBg = when {
        isActionKey -> palette.actionKeyBackground
        isSelected -> palette.actionKeyBackground
        isFunctionKey -> palette.functionKeyBackground
        else -> palette.keyBackground
    }

    val pressedBg = when {
        isActionKey -> palette.actionKeyBackground.copy(alpha = 0.8f)
        isSelected -> palette.actionKeyBackground.copy(alpha = 0.8f)
        isFunctionKey -> palette.keyTextColor.copy(alpha = 0.25f)
        else -> palette.keyTextColor.copy(alpha = 0.2f)
    }

    val backgroundColor = if (isPressed) pressedBg else normalBg

    val textColor = when {
        isActionKey -> palette.actionKeyTextColor
        isSelected -> palette.actionKeyTextColor
        else -> palette.keyTextColor
    }

    val borderModifier = if (preferences.showKeyBorders && !isActionKey && !isSelected) {
        Modifier.border(0.6.dp, palette.keyBorderColor, RoundedCornerShape(cornerRadius))
    } else {
        Modifier
    }

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnDrag by rememberUpdatedState(onHorizontalDrag)
    val scope = rememberCoroutineScope()
    val dragStepPx = with(LocalDensity.current) { dragStepDp.toPx() }
    val useGestures = holdRepeat || onHorizontalDrag != null

    val inputModifier = if (useGestures) {
        Modifier.pointerInput(holdRepeat, dragStepPx) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                val press = PressInteraction.Press(down.position)
                interactionSource.tryEmit(press)

                var repeatJob: Job? = null
                if (holdRepeat) {
                    performFeedback(view, preferences)
                    currentOnClick()
                    repeatJob = scope.launch {
                        delay(400)
                        while (true) {
                            currentOnClick()
                            delay(45)
                        }
                    }
                }

                var accumulated = 0f
                var dragged = false
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) {
                        change.consume()
                        break
                    }
                    val dx = change.position.x - change.previousPosition.x
                    val dragCallback = currentOnDrag
                    if (dx != 0f && dragCallback != null) {
                        accumulated += dx
                        if (abs(accumulated) >= dragStepPx) {
                            val steps = (accumulated / dragStepPx).toInt()
                            accumulated -= steps * dragStepPx
                            dragged = true
                            repeatJob?.cancel()
                            dragCallback(steps)
                        }
                    }
                    change.consume()
                }
                repeatJob?.cancel()
                if (!holdRepeat && !dragged) {
                    performFeedback(view, preferences)
                    currentOnClick()
                }
                interactionSource.tryEmit(PressInteraction.Release(press))
            }
        }
    } else {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                performFeedback(view, preferences)
                onClick()
            },
            onLongClick = onLongClick?.let {
                {
                    performFeedback(view, preferences, isLongPress = true)
                    it()
                }
            }
        )
    }

    val showPopup = isPressed &&
        preferences.keyPopupEnabled &&
        !isFunctionKey && !isActionKey &&
        keyAction.type == KeyType.CHARACTER &&
        keyAction.displayText.length <= 2 &&
        keyAction.displayText.isNotBlank()

    // Outer box is not clipped, so the preview bubble can rise above the key
    Box(
        modifier = modifier
            .weight(keyAction.weight)
            .fillMaxHeight()
            .padding(horizontal = 2.dp, vertical = 3.dp)
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(borderModifier)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .then(inputModifier),
        contentAlignment = Alignment.Center
    ) {
        // Main Text / Icon representation
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = keyAction.displayText,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = keyAction.displayText,
                color = textColor,
                fontSize = if (textSize != TextUnit.Unspecified) textSize
                else if (keyAction.displayText.length > 3) 14.sp else 19.sp,
                fontWeight = if (isActionKey || isFunctionKey) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }

        // Upper-right hint character (e.g., "1" on "q", "@" on "a")
        if (!palette.translucent && !keyAction.hintText.isNullOrEmpty() && !isActionKey && !isFunctionKey) {
            Text(
                text = keyAction.hintText,
                color = palette.hintTextColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp)
            )
        }
    }

    if (showPopup) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-58).dp)
                .zIndex(10f)
                .requiredSize(width = 52.dp, height = 56.dp)
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .background(
                    if (palette.isDark) Color(0xFF4A4D52) else Color.White,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = keyAction.displayText,
                color = if (palette.isDark) Color.White else Color(0xFF202124),
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
    }
}

private fun performFeedback(view: View, preferences: KeyboardPreferences, isLongPress: Boolean = false) {
    if (preferences.vibrateOnKeypress) {
        val feedbackConstant = if (isLongPress) {
            HapticFeedbackConstants.LONG_PRESS
        } else {
            HapticFeedbackConstants.KEYBOARD_TAP
        }
        view.performHapticFeedback(feedbackConstant)
    }
    if (preferences.soundOnKeypress) {
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }
}
