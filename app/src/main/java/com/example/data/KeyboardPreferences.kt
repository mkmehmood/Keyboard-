package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class KeyboardTheme(val id: String, val title: String) {
    DARK_GRAY("dark_gray", "Gboard Dark"),
    MATERIAL_YOU("material_you", "Material You"),
    AMOLED_BLACK("amoled_black", "AMOLED Black"),
    LIGHT_CLASSIC("light_classic", "Gboard Light"),
    GOOGLE_BLUE("google_blue", "Google Blue"),
    MINT_GREEN("mint_green", "Mint Green"),
    CORAL_PINK("coral_pink", "Coral Sunset"),
    CUSTOM("custom", "My Theme")
}

class KeyboardPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gboard_prefs", Context.MODE_PRIVATE)

    var theme: KeyboardTheme
        get() {
            val id = prefs.getString("theme", KeyboardTheme.DARK_GRAY.id)
            return KeyboardTheme.values().firstOrNull { it.id == id } ?: KeyboardTheme.DARK_GRAY
        }
        set(value) = prefs.edit().putString("theme", value.id).apply()

    var showKeyBorders: Boolean
        get() = prefs.getBoolean("show_key_borders", true)
        set(value) = prefs.edit().putBoolean("show_key_borders", value).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean("show_number_row", true)
        set(value) = prefs.edit().putBoolean("show_number_row", value).apply()

    var vibrateOnKeypress: Boolean
        get() = prefs.getBoolean("vibrate_on_keypress", true)
        set(value) = prefs.edit().putBoolean("vibrate_on_keypress", value).apply()

    var vibrateIntensity: Float
        get() = prefs.getFloat("vibrate_intensity", 0.5f)
        set(value) = prefs.edit().putFloat("vibrate_intensity", value).apply()

    var soundOnKeypress: Boolean
        get() = prefs.getBoolean("sound_on_keypress", false)
        set(value) = prefs.edit().putBoolean("sound_on_keypress", value).apply()

    var autoCapitalize: Boolean
        get() = prefs.getBoolean("auto_capitalize", true)
        set(value) = prefs.edit().putBoolean("auto_capitalize", value).apply()

    var showSuggestions: Boolean
        get() = prefs.getBoolean("show_suggestions", true)
        set(value) = prefs.edit().putBoolean("show_suggestions", value).apply()

    var autoCaptureClipboard: Boolean
        get() = prefs.getBoolean("auto_capture_clipboard", true)
        set(value) = prefs.edit().putBoolean("auto_capture_clipboard", value).apply()

    var keyPopupEnabled: Boolean
        get() = prefs.getBoolean("key_popup_enabled", true)
        set(value) = prefs.edit().putBoolean("key_popup_enabled", value).apply()

    var longPressDelayMs: Long
        get() = prefs.getLong("long_press_delay_ms", 300L)
        set(value) = prefs.edit().putLong("long_press_delay_ms", value).apply()

    var autoCorrect: Boolean
        get() = prefs.getBoolean("auto_correct", true)
        set(value) = prefs.edit().putBoolean("auto_correct", value).apply()

    var glideTyping: Boolean
        get() = prefs.getBoolean("glide_typing", true)
        set(value) = prefs.edit().putBoolean("glide_typing", value).apply()

    var doubleSpacePeriod: Boolean
        get() = prefs.getBoolean("double_space_period", true)
        set(value) = prefs.edit().putBoolean("double_space_period", value).apply()

    /** ARGB ints for the user-built theme: background, key, accent. */
    var customBackground: Int
        get() = prefs.getInt("custom_bg", 0xFF1E1F22.toInt())
        set(value) = prefs.edit().putInt("custom_bg", value).apply()

    var customKey: Int
        get() = prefs.getInt("custom_key", 0xFF2B2D31.toInt())
        set(value) = prefs.edit().putInt("custom_key", value).apply()

    var customAccent: Int
        get() = prefs.getInt("custom_accent", 0xFF8AB4F8.toInt())
        set(value) = prefs.edit().putInt("custom_accent", value).apply()
}
