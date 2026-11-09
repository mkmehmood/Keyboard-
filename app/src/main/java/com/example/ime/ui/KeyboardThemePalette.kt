package com.example.ime.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardTheme

data class KeyboardPalette(
    val keyboardBackground: Color,
    val keyBackground: Color,
    val functionKeyBackground: Color,
    val actionKeyBackground: Color,
    val actionKeyTextColor: Color,
    val keyTextColor: Color,
    val hintTextColor: Color,
    val keyBorderColor: Color,
    val stripBackground: Color,
    val stripIconColor: Color,
    val chipBackground: Color,
    val chipTextColor: Color,
    val isDark: Boolean
)

object KeyboardThemePalettes {
    /** Palette for the active preference, including the user-built theme. */
    fun getPalette(prefs: KeyboardPreferences): KeyboardPalette =
        if (prefs.theme == KeyboardTheme.CUSTOM)
            fromColors(Color(prefs.customBackground), Color(prefs.customKey), Color(prefs.customAccent))
        else getPalette(prefs.theme)

    fun fromColors(background: Color, key: Color, accent: Color): KeyboardPalette {
        val dark = background.luminance() < 0.5f
        val text = if (dark) Color(0xFFE8EAED) else Color(0xFF202124)
        val hint = if (dark) Color(0xFF9AA0A6) else Color(0xFF70757A)
        val onAccent = if (accent.luminance() < 0.5f) Color.White else Color(0xFF111111)
        val border = if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.12f)
        return KeyboardPalette(
            keyboardBackground = background,
            keyBackground = key,
            functionKeyBackground = key.copy(alpha = 0.7f),
            actionKeyBackground = accent,
            actionKeyTextColor = onAccent,
            keyTextColor = text,
            hintTextColor = hint,
            keyBorderColor = border,
            stripBackground = background,
            stripIconColor = hint,
            chipBackground = key,
            chipTextColor = text,
            isDark = dark
        )
    }

    fun getPalette(theme: KeyboardTheme): KeyboardPalette {
        return when (theme) {
            KeyboardTheme.DARK_GRAY, KeyboardTheme.CUSTOM -> KeyboardPalette(
                keyboardBackground = Color(0xFF1E1F22),
                keyBackground = Color(0xFF2B2D31),
                functionKeyBackground = Color(0xFF24262A),
                actionKeyBackground = Color(0xFF8AB4F8), // Google Light Blue
                actionKeyTextColor = Color(0xFF0D1D3A),
                keyTextColor = Color(0xFFE8EAED),
                hintTextColor = Color(0xFF9AA0A6),
                keyBorderColor = Color(0xFF383A40),
                stripBackground = Color(0xFF1E1F22),
                stripIconColor = Color(0xFFBDC1C6),
                chipBackground = Color(0xFF303338),
                chipTextColor = Color(0xFFE8EAED),
                isDark = true
            )
            KeyboardTheme.AMOLED_BLACK -> KeyboardPalette(
                keyboardBackground = Color(0xFF000000),
                keyBackground = Color(0xFF121212),
                functionKeyBackground = Color(0xFF1A1A1A),
                actionKeyBackground = Color(0xFF4285F4),
                actionKeyTextColor = Color(0xFFFFFFFF),
                keyTextColor = Color(0xFFFFFFFF),
                hintTextColor = Color(0xFF888888),
                keyBorderColor = Color(0xFF2A2A2A),
                stripBackground = Color(0xFF000000),
                stripIconColor = Color(0xFFCCCCCC),
                chipBackground = Color(0xFF1F1F1F),
                chipTextColor = Color(0xFFFFFFFF),
                isDark = true
            )
            KeyboardTheme.LIGHT_CLASSIC -> KeyboardPalette(
                keyboardBackground = Color(0xFFECEFF1),
                keyBackground = Color(0xFFFFFFFF),
                functionKeyBackground = Color(0xFFCFD8DC),
                actionKeyBackground = Color(0xFF1A73E8),
                actionKeyTextColor = Color(0xFFFFFFFF),
                keyTextColor = Color(0xFF202124),
                hintTextColor = Color(0xFF70757A),
                keyBorderColor = Color(0xFFB0BEC5),
                stripBackground = Color(0xFFECEFF1),
                stripIconColor = Color(0xFF5F6368),
                chipBackground = Color(0xFFFFFFFF),
                chipTextColor = Color(0xFF202124),
                isDark = false
            )
            KeyboardTheme.MATERIAL_YOU -> KeyboardPalette(
                keyboardBackground = Color(0xFF202529),
                keyBackground = Color(0xFF2E353B),
                functionKeyBackground = Color(0xFF252B30),
                actionKeyBackground = Color(0xFFA8C7FA),
                actionKeyTextColor = Color(0xFF062E6F),
                keyTextColor = Color(0xFFE2E2E6),
                hintTextColor = Color(0xFF8E9196),
                keyBorderColor = Color(0xFF3B434A),
                stripBackground = Color(0xFF202529),
                stripIconColor = Color(0xFFC4C7C5),
                chipBackground = Color(0xFF323B42),
                chipTextColor = Color(0xFFE2E2E6),
                isDark = true
            )
            KeyboardTheme.GOOGLE_BLUE -> KeyboardPalette(
                keyboardBackground = Color(0xFF0D253F),
                keyBackground = Color(0xFF17375E),
                functionKeyBackground = Color(0xFF112E50),
                actionKeyBackground = Color(0xFF64B5F6),
                actionKeyTextColor = Color(0xFF0A1E37),
                keyTextColor = Color(0xFFE3F2FD),
                hintTextColor = Color(0xFF90CAF9),
                keyBorderColor = Color(0xFF1E4675),
                stripBackground = Color(0xFF0D253F),
                stripIconColor = Color(0xFFBBDEFB),
                chipBackground = Color(0xFF17375E),
                chipTextColor = Color(0xFFE3F2FD),
                isDark = true
            )
            KeyboardTheme.MINT_GREEN -> KeyboardPalette(
                keyboardBackground = Color(0xFF132A24),
                keyBackground = Color(0xFF1D3E35),
                functionKeyBackground = Color(0xFF17332C),
                actionKeyBackground = Color(0xFF69F0AE),
                actionKeyTextColor = Color(0xFF08271C),
                keyTextColor = Color(0xFFE8F5E9),
                hintTextColor = Color(0xFFA5D6A7),
                keyBorderColor = Color(0xFF2A5449),
                stripBackground = Color(0xFF132A24),
                stripIconColor = Color(0xFFC8E6C9),
                chipBackground = Color(0xFF1D3E35),
                chipTextColor = Color(0xFFE8F5E9),
                isDark = true
            )
            KeyboardTheme.CORAL_PINK -> KeyboardPalette(
                keyboardBackground = Color(0xFF2B181E),
                keyBackground = Color(0xFF40232C),
                functionKeyBackground = Color(0xFF331C23),
                actionKeyBackground = Color(0xFFFF8A80),
                actionKeyTextColor = Color(0xFF3E0E18),
                keyTextColor = Color(0xFFFFEBEE),
                hintTextColor = Color(0xFFFFCDD2),
                keyBorderColor = Color(0xFF59303D),
                stripBackground = Color(0xFF2B181E),
                stripIconColor = Color(0xFFFFCDD2),
                chipBackground = Color(0xFF40232C),
                chipTextColor = Color(0xFFFFEBEE),
                isDark = true
            )
        }
    }
}
