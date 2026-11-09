package com.example.ime.ui

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Box(
        modifier = modifier
            .weight(keyAction.weight)
            .fillMaxHeight()
            .padding(horizontal = 2.dp, vertical = 3.dp)
            .then(borderModifier)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .combinedClickable(
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
            ),
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
