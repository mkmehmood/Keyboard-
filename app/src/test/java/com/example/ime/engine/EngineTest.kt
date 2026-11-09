package com.example.ime.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {
    private val rows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")

    @Test fun prefixSuggestionsKeepCase() {
        val e = SuggestionEngine()
        assertEquals("the", e.suggest("th", null).first())
        assertEquals("The", e.suggest("Th", null).first())
    }

    @Test fun autocorrectFixesTyposOnly() {
        val e = SuggestionEngine()
        assertEquals("the", e.correction("teh"))
        assertNull(e.correction("hello"))
        assertNull(e.correction("ab"))
    }

    @Test fun learnsWordsAndNextWords() {
        val e = SuggestionEngine()
        e.learn("zorblax", null)
        assertTrue(e.suggest("zor", null).contains("zorblax"))
        e.learn("coffee", "morning")
        assertEquals("coffee", e.suggest("", "morning").first())
        e.forget("zorblax")
        assertFalse(e.isKnown("zorblax"))
    }

    @Test fun nonEnglishUsesOnlyUserWords() {
        val e = SuggestionEngine()
        assertTrue(e.suggest("th", null, userOnly = true).isEmpty())
        e.learn("thüringen", null)
        assertEquals(listOf("thüringen"), e.suggest("th", null, userOnly = true))
    }

    @Test fun glideDecodesWordsFromKeyPath() {
        val e = SuggestionEngine()
        val keys = KeyGeometry.centers(rows, 1080f, 2.75f, hasNumberRow = true)
        val path = "hello".map { keys.getValue(it) }
        val letters = GlideDecoder.lettersFromPath(path, keys)
        assertEquals("helo", letters)
        assertEquals("hello", GlideDecoder.candidates(letters, e).first())
        assertEquals("thank", GlideDecoder.candidates("thank", e).first())
    }
}
