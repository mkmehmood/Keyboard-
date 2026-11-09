package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ClipboardItem
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Gboard", appName)
    }

    @Test
    fun `test keyboard preferences default and updates`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = KeyboardPreferences(context)
        preferences.theme = KeyboardTheme.AMOLED_BLACK
        assertEquals(KeyboardTheme.AMOLED_BLACK, preferences.theme)

        preferences.showKeyBorders = true
        assertTrue(preferences.showKeyBorders)
    }

    @Test
    fun `test clipboard item creation`() {
        val item = ClipboardItem(
            text = "Hello unlimited copy paste!",
            isPinned = true,
            category = "General"
        )
        assertEquals("Hello unlimited copy paste!", item.text)
        assertTrue(item.isPinned)
        assertEquals(27, item.characterCount)
    }

    @Test
    fun `test Room database insert and query clips`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.clipboardDao()

        val item1 = ClipboardItem(text = "Snippet 1", category = "Code")
        val item2 = ClipboardItem(text = "Snippet 2", isPinned = true, category = "Email")
        dao.insert(item1)
        dao.insert(item2)

        val retrieved1 = dao.findByText("Snippet 1")
        assertNotNull(retrieved1)
        assertEquals("Code", retrieved1?.category)

        val pinned = dao.getPinnedClips().first()
        assertEquals(1, pinned.size)
        assertEquals("Snippet 2", pinned[0].text)

        val searchResult = dao.searchClips("Snippet").first()
        assertEquals(2, searchResult.size)

        db.close()
    }
}
