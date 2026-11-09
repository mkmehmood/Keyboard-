package com.example.ime.engine

data class Pt(val x: Float, val y: Float)

/** Turns a finger path over the letter keys into ranked word candidates. */
object GlideDecoder {

    fun lettersFromPath(points: List<Pt>, keys: Map<Char, Pt>): String {
        val sb = StringBuilder()
        for (p in points) {
            val k = keys.minByOrNull { (_, c) ->
                val dx = c.x - p.x
                val dy = c.y - p.y
                dx * dx + dy * dy
            }?.key ?: continue
            if (sb.isEmpty() || sb.last() != k) sb.append(k)
        }
        return sb.toString()
    }

    fun candidates(path: String, engine: SuggestionEngine, limit: Int = 5): List<String> {
        if (path.length < 2) return emptyList()
        return engine.allWords().asSequence()
            .filter { it.length >= 2 && it.all { c -> c in 'a'..'z' } }
            .filter { it.first() == path.first() && it.last() == path.last() }
            .filter { isSubsequence(collapse(it), path) }
            .map { w -> w to (collapse(w).length.toFloat() / path.length * 1000f + engine.score(w)) }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
            .toList()
    }

    private fun collapse(w: String): String {
        val sb = StringBuilder()
        for (c in w) if (sb.isEmpty() || sb.last() != c) sb.append(c)
        return sb.toString()
    }

    private fun isSubsequence(needle: String, hay: String): Boolean {
        var i = 0
        for (c in hay) if (i < needle.length && needle[i] == c) i++
        return i == needle.length
    }
}

/** Approximate key centres for the QWERTY page, matching the Row sizes used in KeyboardLayoutView. */
object KeyGeometry {
    fun centers(
        rows: List<String>, // three letter rows, e.g. "qwertyuiop","asdfghjkl","zxcvbnm"
        widthPx: Float,
        density: Float,
        hasNumberRow: Boolean,
    ): Map<Char, Pt> {
        val pad = 2f * density
        val gap = 4f * density
        val rowH = 44f * density
        val numH = if (hasNumberRow) 42f * density else 0f
        var y = pad + (if (hasNumberRow) numH + gap else 0f)
        val inner = widthPx - 2 * pad
        val out = HashMap<Char, Pt>()

        fun place(letters: String, left: Float, width: Float, y0: Float) {
            val kw = width / letters.length
            letters.forEachIndexed { i, c -> out[c] = Pt(left + kw * (i + 0.5f), y0 + rowH / 2) }
        }

        place(rows[0], pad, inner, y); y += rowH + gap
        val indent = if (rows[1].length >= 10) 0f else 14f * density
        place(rows[1], pad + indent, inner - 2 * indent, y); y += rowH + gap
        val side = 1.35f
        val total = rows[2].length + 2 * side
        val unit = inner / total
        place(rows[2], pad + unit * side, unit * rows[2].length, y)
        return out
    }

    /** Y coordinate of the bottom edge of the third letter row. */
    fun lettersBottom(density: Float, hasNumberRow: Boolean): Float {
        val gap = 4f * density
        val rowH = 44f * density
        val numH = if (hasNumberRow) 42f * density + gap else 0f
        return 2f * density + numH + 3 * rowH + 2 * gap
    }
}
