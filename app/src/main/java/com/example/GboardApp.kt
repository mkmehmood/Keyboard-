package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ClipboardRepository
import com.example.data.KeyboardPreferences

class GboardApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var clipboardRepository: ClipboardRepository
        private set

    lateinit var keyboardPreferences: KeyboardPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        clipboardRepository = ClipboardRepository(this, database.clipboardDao())
        keyboardPreferences = KeyboardPreferences(this)
    }

    companion object {
        lateinit var instance: GboardApp
            private set
    }
}
