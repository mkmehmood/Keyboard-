package com.example.ime.engine

/**
 * Prefix suggestions, next-word prediction, and one-edit autocorrect.
 * Pure Kotlin (no Android types) so it can be unit tested on the JVM.
 */
class SuggestionEngine(baseWords: List<String> = WordList.COMMON) {

    private val rank = HashMap<String, Int>()
    private val userCounts = HashMap<String, Int>()
    private val bigrams = HashMap<String, HashMap<String, Int>>()
    private val blocked = HashSet<String>()

    init {
        baseWords.forEachIndexed { i, w -> rank.putIfAbsent(w.lowercase(), i) }
    }

    /** Replace the learned vocabulary (for example loaded from the database). */
    @Synchronized
    fun setUserWords(words: Map<String, Int>) {
        userCounts.clear()
        words.forEach { (w, c) -> userCounts[w.lowercase()] = c }
    }

    @Synchronized
    fun learn(word: String, previous: String?) {
        val w = word.lowercase()
        if (w.length < 2 || w in blocked || !w.any { it.isLetter() }) return
        userCounts[w] = (userCounts[w] ?: 0) + 1
        val p = previous?.lowercase()
        if (!p.isNullOrEmpty()) {
            val m = bigrams.getOrPut(p) { HashMap() }
            m[w] = (m[w] ?: 0) + 1
        }
    }

    @Synchronized
    fun forget(word: String) {
        val w = word.lowercase()
        userCounts.remove(w)
        bigrams.values.forEach { it.remove(w) }
    }

    @Synchronized
    fun isKnown(word: String): Boolean {
        val w = word.lowercase()
        return w in rank || w in userCounts
    }

    @Synchronized
    fun allWords(): List<String> = (rank.keys + userCounts.keys).distinct()

    /** Higher is more likely. */
    @Synchronized
    fun score(word: String): Int {
        val w = word.lowercase()
        val base = rank[w]?.let { 1000 - minOf(it, 999) } ?: 0
        return base + (userCounts[w] ?: 0) * 50
    }

    @Synchronized
    fun suggest(prefix: String, previous: String?, limit: Int = 3, userOnly: Boolean = false): List<String> {
        val p = prefix.lowercase()
        val prev = previous?.lowercase()
        val next = if (prev != null) bigrams[prev] else null

        val scored: List<Pair<String, Int>> =
            if (p.isEmpty()) {
                val fromBigrams = next?.map { (w, c) -> w to 2000 + c * 200 } ?: emptyList()
                val fallback = if (userOnly) emptyList() else listOf("i", "the", "to", "and", "a").map { it to 1 }
                (fromBigrams + fallback)
            } else {
                (if (userOnly) userCounts.keys.toList() else allWords()).asSequence()
                    .filter { it.startsWith(p) && it != p }
                    .map { w -> w to (score(w) + (next?.get(w) ?: 0) * 200) }
                    .toList()
            }

        return scored.sortedByDescending { it.second }
            .map { applyCase(it.first, prefix) }
            .distinct()
            .take(limit)
    }

    /** Returns a replacement for a misspelled word, or null if no confident fix exists. */
    @Synchronized
    fun correction(word: String): String? {
        val w = word.lowercase()
        if (w.length < 3 || isKnown(w) || w.any { !it.isLetter() && it != '\'' }) return null
        val best = allWords().asSequence()
            .filter { kotlin.math.abs(it.length - w.length) <= 1 && withinOneEdit(w, it) }
            .maxByOrNull { score(it) } ?: return null
        return applyCase(best, word)
    }

    private fun applyCase(candidate: String, typed: String): String = when {
        candidate == "i" -> "I"
        typed.length > 1 && typed.all { it.isUpperCase() } -> candidate.uppercase()
        typed.isNotEmpty() && typed[0].isUpperCase() -> candidate.replaceFirstChar { it.uppercase() }
        else -> candidate
    }

    companion object {
        /** True when a and b differ by at most one insert, delete, replace or adjacent swap. */
        fun withinOneEdit(a: String, b: String): Boolean {
            if (a == b) return true
            val la = a.length
            val lb = b.length
            if (kotlin.math.abs(la - lb) > 1) return false
            var i = 0
            while (i < la && i < lb && a[i] == b[i]) i++
            if (la == lb) {
                if (a.substring(i + 1) == b.substring(i + 1)) return true // replace
                return i + 1 < la && a[i] == b[i + 1] && a[i + 1] == b[i] &&
                    a.substring(i + 2) == b.substring(i + 2) // swap
            }
            return if (la > lb) a.substring(i + 1) == b.substring(i) else b.substring(i + 1) == a.substring(i)
        }
    }
}
