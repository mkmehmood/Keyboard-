package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KeyboardSettingsScreen
import com.example.ui.PermissionsScreen
import com.example.ui.SetupWizardScreen
import com.example.ui.UnlimitedClipboardScreen
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.MyApplicationTheme

enum class DashboardTab(val title: String, val icon: ImageVector) {
    SETUP("Setup & Test", Icons.Default.Keyboard),
    CLIPBOARD("Clipboard", Icons.Default.ContentPaste),
    PREFERENCES("Themes & Keys", Icons.Default.Tune),
    PERMISSIONS("Permissions", Icons.Default.Security)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = applicationContext as GboardApp
        val repository = app.clipboardRepository
        val preferences = app.keyboardPreferences

        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableStateOf(DashboardTab.SETUP) }

                BackHandler(enabled = selectedTab != DashboardTab.SETUP) {
                    selectedTab = DashboardTab.SETUP
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        GboardTopBar(
                            currentTab = selectedTab
                        )
                    },
                    bottomBar = {
                        GboardNavigationBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            DashboardTab.SETUP -> SetupWizardScreen(
                                onNavigateToClipboard = { selectedTab = DashboardTab.CLIPBOARD }
                            )
                            DashboardTab.CLIPBOARD -> UnlimitedClipboardScreen(
                                repository = repository
                            )
                            DashboardTab.PREFERENCES -> KeyboardSettingsScreen(
                                preferences = preferences
                            )
                            DashboardTab.PERMISSIONS -> PermissionsScreen()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GboardTopBar(
    currentTab: DashboardTab
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Stylized Gboard Logo
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoogleBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Gboard",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentTab.title,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun GboardNavigationBar(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.testTag("gboard_navigation_bar")
    ) {
        DashboardTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
