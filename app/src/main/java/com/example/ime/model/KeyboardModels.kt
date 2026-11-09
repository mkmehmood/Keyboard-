package com.example.ime.model

sealed class KeyboardMode {
    object QWERTY : KeyboardMode()
    object SYMBOLS_1 : KeyboardMode()
    object SYMBOLS_2 : KeyboardMode()
    object EMOJI : KeyboardMode()
    object CLIPBOARD : KeyboardMode()
    object TEXT_EDIT : KeyboardMode()
}

enum class ShiftState {
    LOWERCASE,
    UPPERCASE,
    CAPS_LOCKED
}

data class KeyAction(
    val primaryText: String,
    val displayText: String = primaryText,
    val hintText: String? = null,
    val type: KeyType = KeyType.CHARACTER,
    val weight: Float = 1.0f
)

enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    MODE_CHANGE,
    EMOJI_TOGGLE,
    CLIPBOARD_TOGGLE,
    SETTINGS_SHORTCUT
}

enum class LayoutKind(val rows: List<String>) {
    QWERTY(listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")),
    QWERTZ(listOf("qwertzuiop", "asdfghjkl", "yxcvbnm")),
    AZERTY(listOf("azertyuiop", "qsdfghjklm", "wxcvbn"));

    companion object {
        fun forLocale(tag: String?): LayoutKind = when (tag?.lowercase()?.take(2)) {
            "de" -> QWERTZ
            "fr" -> AZERTY
            else -> QWERTY
        }
    }
}

object KeyboardPresets {
    private val hintByLetter: Map<String, String?> by lazy {
        (qwertyRow1 + qwertyRow2 + qwertyRow3).associate { it.primaryText to it.hintText }
    }

    /** Letter rows for a layout, reusing the long-press hints of the QWERTY keys. */
    fun rowsFor(layout: LayoutKind): List<List<KeyAction>> =
        layout.rows.map { row -> row.map { c -> KeyAction(c.toString(), hintText = hintByLetter[c.toString()]) } }

    val numberRow = listOf(
        KeyAction("1", hintText = "~"),
        KeyAction("2", hintText = "@"),
        KeyAction("3", hintText = "#"),
        KeyAction("4", hintText = "$"),
        KeyAction("5", hintText = "%"),
        KeyAction("6", hintText = "^"),
        KeyAction("7", hintText = "&"),
        KeyAction("8", hintText = "*"),
        KeyAction("9", hintText = "("),
        KeyAction("0", hintText = ")")
    )

    val qwertyRow1 = listOf(
        KeyAction("q", hintText = "1"),
        KeyAction("w", hintText = "2"),
        KeyAction("e", hintText = "3"),
        KeyAction("r", hintText = "4"),
        KeyAction("t", hintText = "5"),
        KeyAction("y", hintText = "6"),
        KeyAction("u", hintText = "7"),
        KeyAction("i", hintText = "8"),
        KeyAction("o", hintText = "9"),
        KeyAction("p", hintText = "0")
    )

    val qwertyRow2 = listOf(
        KeyAction("a", hintText = "@"),
        KeyAction("s", hintText = "#"),
        KeyAction("d", hintText = "$"),
        KeyAction("f", hintText = "_"),
        KeyAction("g", hintText = "&"),
        KeyAction("h", hintText = "-"),
        KeyAction("j", hintText = "+"),
        KeyAction("k", hintText = "("),
        KeyAction("l", hintText = ")")
    )

    val qwertyRow3 = listOf(
        KeyAction("z", hintText = "*"),
        KeyAction("x", hintText = "\""),
        KeyAction("c", hintText = "'"),
        KeyAction("v", hintText = ":"),
        KeyAction("b", hintText = ";"),
        KeyAction("n", hintText = "!"),
        KeyAction("m", hintText = "?")
    )

    // Symbols page 1
    val symbolsRow1 = listOf(
        KeyAction("@"), KeyAction("#"), KeyAction("$"), KeyAction("_"), KeyAction("&"),
        KeyAction("-"), KeyAction("+"), KeyAction("("), KeyAction(")"), KeyAction("/")
    )
    val symbolsRow2 = listOf(
        KeyAction("*"), KeyAction("\""), KeyAction("'"), KeyAction(":"), KeyAction(";"),
        KeyAction("!"), KeyAction("?"), KeyAction("\\"), KeyAction("%")
    )
    val symbolsRow3 = listOf(
        KeyAction("~"), KeyAction("|"), KeyAction("<"), KeyAction(">"),
        KeyAction("="), KeyAction("{"), KeyAction("}")
    )

    // Symbols page 2
    val symbolsPage2Row1 = listOf(
        KeyAction("`"), KeyAction("^"), KeyAction("°"), KeyAction("§"), KeyAction("€"),
        KeyAction("£"), KeyAction("¥"), KeyAction("₹"), KeyAction("¢"), KeyAction("©")
    )
    val symbolsPage2Row2 = listOf(
        KeyAction("["), KeyAction("]"), KeyAction("™"), KeyAction("✓"), KeyAction("•"),
        KeyAction("÷"), KeyAction("×"), KeyAction("¶"), KeyAction("∆")
    )
    val symbolsPage2Row3 = listOf(
        KeyAction("«"), KeyAction("»"), KeyAction("≤"), KeyAction("≥"),
        KeyAction("≠"), KeyAction("¿"), KeyAction("¡")
    )

    val emojiCategories = mapOf(
        "Smileys" to listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹", "😊", "😇",
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛",
            "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🥸", "🤩", "🥳", "😏", "😒",
            "😞", "😔", "😟", "😕", "🙁", "😣", "😖", "😫", "😩", "🥺", "😢", "😭",
            "😮‍💨", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰",
            "🤔", "🫣", "🫢", "🫡", "🤫", "🫠", "🤐", "😐", "😑", "😶", "🫥", "😬"
        ),
        "Gestures" to listOf(
            "👍", "👎", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✍️", "💪", "👊", "🤛",
            "🤜", "🤞", "✌️", "🤟", "🤘", "👌", "🤌", "🤏", "👈", "👉", "👆", "👇",
            "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "🤙", "🫵", "❤️", "🧡", "💛", "💚",
            "💙", "💜", "🖤", "🤍", "🤎", "💔", "❤️‍🔥", "💯", "💢", "💥", "💫", "✨"
        ),
        "Nature" to listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨", "🐯", "🦁",
            "🐮", "🐷", "🐸", "🐵", "🙈", "🙉", "🙊", "🐒", "🐔", "🐧", "🐦", "🐤",
            "🦆", "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🪱", "🐛", "🦋",
            "🐌", "🐞", "🐜", "🪰", "🪲", "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠",
            "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "🍀", "🍁", "🍂", "🍃", "🌸", "🌺", "🌹"
        ),
        "Food" to listOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒",
            "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🥑", "🥦", "🌽", "🥕", "🧄", "🧅",
            "🥔", "🍞", "🥐", "🥖", "🥨", "🥯", "🧀", "🥚", "🍳", "🧈", "🥞", "🧇",
            "🥓", "🥩", "🍗", "🍖", "🌭", "🍔", "🍟", "🍕", "🥪", "🌮", "🌯", "🍜",
            "🍝", "🍣", "🍱", "🥟", "🍦", "🍧", "🍨", "🍩", "🍪", "🎂", "🍰", "🍫", "🍿"
        ),
        "Objects" to listOf(
            "⌚", "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "📷", "📸", "📹", "📺",
            "📻", "🎙️", "📞", "☎️", "📟", "🔋", "🔌", "💡", "🔦", "🕯️", "💴", "💵",
            "💶", "💷", "🪙", "💰", "💳", "💎", "⚖️", "🔧", "🔨", "⚒️", "🛠️", "⛏️",
            "🔩", "⚙️", "🔗", "⛓️", "🧲", "🔫", "💣", "🧨", "🔪", "🗡️", "⚔️", "🛡️",
            "🔑", "🗝️", "🚪", "🪑", "🛋️", "🛏️", "📦", "📫", "📦", "🏷️", "🔖", "📌", "📍"
        )
    )
}
