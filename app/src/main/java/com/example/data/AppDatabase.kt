package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ClipboardItem::class, UserWord::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao
    abstract fun userWordDao(): UserWordDao

    companion object {
        /** 2 -> 3 only adds the user dictionary table, so clipboard history is kept. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS user_words (" +
                        "word TEXT NOT NULL, count INTEGER NOT NULL, lastUsed INTEGER NOT NULL, " +
                        "PRIMARY KEY(word))"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gboard_keyboard_database.db"
                ).addMigrations(MIGRATION_2_3).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
