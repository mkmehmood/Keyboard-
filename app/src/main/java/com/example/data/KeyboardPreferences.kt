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
    PHOTO("photo", "My Photo")
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

    /** Absolute path of the custom keyboard background photo (copied into app storage), or null. */
    var backgroundImagePath: String?
        get() = prefs.getString("background_image_path", null)
        set(value) = prefs.edit().putString("background_image_path", value).apply()

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
}
