package com.example.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.GboardApp
import com.example.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
class UserDictionaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as GboardApp).userDictionary
        setContent {
            MyApplicationTheme {
                val words by repo.words.collectAsState(initial = emptyList())
                var input by remember { mutableStateOf("") }
                var confirmClear by remember { mutableStateOf(false) }

                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text("Dictionary") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            if (words.isNotEmpty()) TextButton(onClick = { confirmClear = true }) { Text("Clear all") }
                        },
                    )
                }) { pad ->
                    Column(Modifier.padding(pad).fillMaxSize()) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = input,
                                onValueChange = { input = it.filter { c -> c.isLetter() || c == '\'' } },
                                label = { Text("Add a word") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(8.dp))
                            FilledIconButton(
                                onClick = { if (input.length >= 2) { repo.add(input); input = "" } },
                            ) { Icon(Icons.Default.Add, contentDescription = "Add") }
                        }
                        if (words.isEmpty()) {
                            Text(
                                "Words you type are learned here automatically. Add your own or remove ones you don't want suggested.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        LazyColumn(Modifier.weight(1f)) {
                            items(words, key = { it.word }) { w ->
                                ListItem(
                                    headlineContent = { Text(w.word) },
                                    supportingContent = { Text("Used ${w.count} times") },
                                    trailingContent = {
                                        IconButton(onClick = { repo.remove(w.word) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove ${w.word}")
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                if (confirmClear) {
                    AlertDialog(
                        onDismissRequest = { confirmClear = false },
                        title = { Text("Clear learned words?") },
                        text = { Text("This removes every word the keyboard has learned. It can't be undone.") },
                        confirmButton = { TextButton(onClick = { repo.clear(); confirmClear = false }) { Text("Clear") } },
                        dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
                    )
                }
            }
        }
    }
}
