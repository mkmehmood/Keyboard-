package com.example.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GboardApp
import com.example.data.KeyboardTheme
import com.example.ime.ui.KeyboardThemePalettes
import com.example.ui.theme.MyApplicationTheme

class ThemeBuilderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = (application as GboardApp).keyboardPreferences
        setContent {
            MyApplicationTheme {
                ThemeBuilder(
                    initialBg = Color(prefs.customBackground),
                    initialKey = Color(prefs.customKey),
                    initialAccent = Color(prefs.customAccent),
                    onBack = { finish() },
                    onSave = { bg, key, accent ->
                        prefs.customBackground = bg.toArgb()
                        prefs.customKey = key.toArgb()
                        prefs.customAccent = accent.toArgb()
                        prefs.theme = KeyboardTheme.CUSTOM
                        finish()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeBuilder(
    initialBg: Color,
    initialKey: Color,
    initialAccent: Color,
    onBack: () -> Unit,
    onSave: (Color, Color, Color) -> Unit,
) {
    var bg by remember { mutableStateOf(initialBg) }
    var key by remember { mutableStateOf(initialKey) }
    var accent by remember { mutableStateOf(initialAccent) }
    val palette = KeyboardThemePalettes.fromColors(bg, key, accent)

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Build your theme") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
    }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Live preview
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(palette.keyboardBackground).padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf("qwertyuiop", "asdfghjkl").forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { c ->
                            Box(
                                Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(palette.keyBackground),
                                contentAlignment = Alignment.Center,
                            ) { Text(c.toString(), color = palette.keyTextColor, fontSize = 15.sp) }
                        }
                    }
                }
                Box(
                    Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(19.dp))
                        .background(palette.actionKeyBackground),
                    contentAlignment = Alignment.Center,
                ) { Text("Enter", color = palette.actionKeyTextColor) }
            }

            ColorControl("Background", bg) { bg = it }
            ColorControl("Keys", key) { key = it }
            ColorControl("Accent", accent) { accent = it }

            Spacer(Modifier.weight(1f))
            Button(onClick = { onSave(bg, key, accent) }, modifier = Modifier.fillMaxWidth()) {
                Text("Save and use")
            }
        }
    }
}

/** Hue + lightness sliders; kept simple on purpose. */
@Composable
private fun ColorControl(label: String, color: Color, onChange: (Color) -> Unit) {
    val hsv = remember {
        FloatArray(3).also { android.graphics.Color.colorToHSV(color.toArgb(), it) }
    }
    var hue by remember { mutableStateOf(hsv[0]) }
    var sat by remember { mutableStateOf(hsv[1]) }
    var value by remember { mutableStateOf(hsv[2]) }
    fun push() = onChange(Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value))))

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).background(color))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 14.sp)
        }
        Slider(value = hue, onValueChange = { hue = it; push() }, valueRange = 0f..360f)
        Slider(value = sat, onValueChange = { sat = it; push() }, valueRange = 0f..1f)
        Slider(value = value, onValueChange = { value = it; push() }, valueRange = 0f..1f)
    }
}
