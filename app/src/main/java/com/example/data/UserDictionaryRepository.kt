package com.example.data

import com.example.ime.engine.SuggestionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Keeps the in-memory [SuggestionEngine] and the Room table in sync. */
class UserDictionaryRepository(
    private val dao: UserWordDao,
    val engine: SuggestionEngine
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    val words: Flow<List<UserWord>> = dao.observeAll()

    fun load() {
        scope.launch { engine.setUserWords(dao.getAll().associate { it.word to it.count }) }
    }

    fun learn(word: String, previous: String?) {
        val w = word.lowercase()
        engine.learn(w, previous)
        scope.launch {
            val now = System.currentTimeMillis()
            dao.ensure(w, now)
            dao.bump(w, now)
        }
    }

    fun add(word: String) = learn(word.trim(), null)

    fun remove(word: String) {
        engine.forget(word)
        scope.launch { dao.delete(word.lowercase()) }
    }

    fun clear() {
        engine.setUserWords(emptyMap())
        scope.launch { dao.clear() }
    }
}
