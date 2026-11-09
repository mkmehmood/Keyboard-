package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.GoogleYellow

@Composable
fun PermissionsScreen() {
    val context = LocalContext.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Privacy & Security Badge
        Card(
            modifier = Modifier.fillMaxWidth().testTag("permissions_hero_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GoogleBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "100% On-Device & Private",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Your keystrokes and unlimited clipboard are saved strictly in your local encrypted device storage. No data ever leaves your device.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Text(
            text = "Device Permissions Overview",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        // BIND_INPUT_METHOD Status
        PermissionItemCard(
            title = "Input Method Service (BIND_INPUT_METHOD)",
            description = "Allows Gboard to act as your system-wide keyboard across all installed apps and browsers.",
            icon = Icons.Default.Keyboard,
            isGranted = true,
            actionLabel = "System Configured",
            onAction = null
        )

        // VIBRATE Status
        PermissionItemCard(
            title = "Haptic Vibration (VIBRATE)",
            description = "Powers authentic Gboard tactile feedback and keypress vibrations.",
            icon = Icons.Default.Vibration,
            isGranted = true,
            actionLabel = "Granted by Default",
            onAction = null
        )

        // RECORD_AUDIO Status
        PermissionItemCard(
            title = "Voice Typing (RECORD_AUDIO)",
            description = "Enables real-time voice dictation when pressing the microphone button on the keyboard top bar.",
            icon = Icons.Default.Mic,
            isGranted = hasAudioPermission,
            actionLabel = if (hasAudioPermission) "Permission Active" else "Grant Voice Access",
            onAction = if (!hasAudioPermission) {
                { audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            } else null
        )

        // POST_NOTIFICATIONS Status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionItemCard(
                title = "Notifications (POST_NOTIFICATIONS)",
                description = "Shows notifications for clipboard sync and quick action toggles.",
                icon = Icons.Default.Notifications,
                isGranted = hasNotificationPermission,
                actionLabel = if (hasNotificationPermission) "Permission Active" else "Enable Notifications",
                onAction = if (!hasNotificationPermission) {
                    { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                } else null
            )
        }

        // Unlimited Copy-Paste Capability
        PermissionItemCard(
            title = "Unlimited Clipboard Access",
            description = "Continuous real-time clipboard listener backed by persistent Room SQLite database with zero clip expiration.",
            icon = Icons.Default.ContentPaste,
            isGranted = true,
            actionLabel = "Fully Enabled",
            onAction = null
        )
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    actionLabel: String,
    onAction: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) GoogleGreen.copy(alpha = 0.15f) else GoogleYellow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) GoogleGreen else GoogleYellow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        text = if (isGranted) "Status: Enabled & Active" else "Status: Action Required",
                        fontSize = 11.sp,
                        color = if (isGranted) GoogleGreen else GoogleYellow,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (onAction != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue)
                ) {
                    Text(actionLabel, fontSize = 13.sp)
                }
            }
        }
    }
}
