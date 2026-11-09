package com.example.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.MainActivity
import com.example.ui.theme.MyApplicationTheme

/** One row in the settings hub (an original design, organised like a typical IME settings page). */
data class SettingsEntry(
    val id: String,
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val tab: String? = null, // DashboardTab name to open in MainActivity
    val target: Class<*>? = null, // or a dedicated screen
)

val settingsEntries = listOf(
    SettingsEntry("languages", "Languages", "English, Español, Français, Deutsch", Icons.Default.Language, "PREFERENCES"),
    SettingsEntry("preferences", "Preferences", "Number row, key popup, haptics, sound", Icons.Default.Tune, "PREFERENCES"),
    SettingsEntry("theme", "Theme", "Presets and your own colours", Icons.Default.Palette, target = ThemeBuilderActivity::class.java),
    SettingsEntry("glide", "Glide typing", "Slide across letters to type", Icons.Default.Gesture, "PREFERENCES"),
    SettingsEntry("correction", "Text correction", "Suggestions and auto-correction", Icons.Default.Spellcheck, "PREFERENCES"),
    SettingsEntry("dictionary", "Dictionary", "Learned words", Icons.Default.MenuBook, target = UserDictionaryActivity::class.java),
    SettingsEntry("clipboard", "Clipboard", "Unlimited history", Icons.Default.ContentPaste, "CLIPBOARD"),
    SettingsEntry("permissions", "Permissions", "What the keyboard can access", Icons.Default.Security, "PERMISSIONS"),
    SettingsEntry("about", "About", "Setup and test field", Icons.Default.Info, "SETUP"),
)

class SettingsHubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SettingsHub(
                    onBack = { finish() },
                    onOpen = { entry ->
                        when {
                            entry.target != null -> startActivity(Intent(this, entry.target))
                            entry.tab != null -> startActivity(
                                Intent(this, MainActivity::class.java)
                                    .putExtra(MainActivity.EXTRA_TAB, entry.tab)
                            )
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHub(onBack: () -> Unit, onOpen: (SettingsEntry) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keyboard settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            items(settingsEntries, key = { it.id }) { e ->
                ListItem(
                    modifier = Modifier.clickable(enabled = e.tab != null || e.target != null) { onOpen(e) },
                    leadingContent = { Icon(e.icon, contentDescription = null) },
                    headlineContent = { Text(e.title) },
                    supportingContent = { Text(e.summary) },
                )
            }
        }
    }
}
