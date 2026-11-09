package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ClipboardRepository
import com.example.data.KeyboardPreferences
import com.example.data.UserDictionaryRepository
import com.example.ime.engine.SuggestionEngine

class GboardApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var clipboardRepository: ClipboardRepository
        private set

    lateinit var keyboardPreferences: KeyboardPreferences
        private set

    lateinit var userDictionary: UserDictionaryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        clipboardRepository = ClipboardRepository(this, database.clipboardDao())
        keyboardPreferences = KeyboardPreferences(this)
        userDictionary = UserDictionaryRepository(database.userWordDao(), SuggestionEngine())
        userDictionary.load()
    }

    companion object {
        lateinit var instance: GboardApp
            private set
    }
}
