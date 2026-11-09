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
    CUSTOM("custom", "My Theme"),
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

    /** Key height multiplier (0.8 = compact, 1.0 = default, 1.3 = tall). */
    var keyboardHeightScale: Float
        get() = prefs.getFloat("keyboard_height_scale", 1.0f)
        set(value) = prefs.edit().putFloat("keyboard_height_scale", value).apply()

    /** Extra space below the keyboard in dp (keeps keys clear of gesture bar / curved edges). */
    var keyboardBottomPaddingDp: Int
        get() = prefs.getInt("keyboard_bottom_padding_dp", 0)
        set(value) = prefs.edit().putInt("keyboard_bottom_padding_dp", value).apply()

    /** "off", "left" or "right": shrink the keys to one side for one-handed typing. */
    var oneHandedMode: String
        get() = prefs.getString("one_handed_mode", "off") ?: "off"
        set(value) = prefs.edit().putString("one_handed_mode", value).apply()

    /** Swipe left on backspace to delete whole words. */
    var gestureDelete: Boolean
        get() = prefs.getBoolean("gesture_delete", true)
        set(value) = prefs.edit().putBoolean("gesture_delete", value).apply()

    /** Swipe along the space bar to move the cursor. */
    var spaceCursorControl: Boolean
        get() = prefs.getBoolean("space_cursor_control", true)
        set(value) = prefs.edit().putBoolean("space_cursor_control", value).apply()

    /** Double-tap space inserts ". ". */
    var doubleSpacePeriod: Boolean
        get() = prefs.getBoolean("double_space_period", true)
        set(value) = prefs.edit().putBoolean("double_space_period", value).apply()

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
