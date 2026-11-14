package com.tobaccoscanner.app

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TobaccoTheme { ScannerApp() } }
    }
}

sealed interface Ui {
    data object Idle : Ui
    data class Scanning(val bmp: Bitmap) : Ui
    data class Done(val bmp: Bitmap, val result: ScanResult) : Ui
    data class Failed(val bmp: Bitmap?, val kind: FailKind, val message: String?) : Ui
}

private fun replyLanguage(): String =
    Locale.getDefault().getDisplayLanguage(Locale.ENGLISH).ifBlank { "English" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerApp() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var apiKey by remember { mutableStateOf(Prefs.key(ctx)) }
    var model by remember { mutableStateOf(Prefs.model(ctx)) }
    var showSettings by remember { mutableStateOf(false) }
    var ui by remember { mutableStateOf<Ui>(Ui.Idle) }
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var lastUri by remember { mutableStateOf<Uri?>(null) }

    fun start(uri: Uri) {
        if (apiKey.isBlank()) {
            showSettings = true
            return
        }
        lastUri = uri
        scope.launch {
            val bmp = withContext(Dispatchers.IO) { ImageUtils.load(ctx, uri) }
            if (bmp == null) {
                ui = Ui.Failed(null, FailKind.OTHER, null)
                return@launch
            }
            ui = Ui.Scanning(bmp)
            ui = try {
                Ui.Done(bmp, ClaudeScanner.scan(bmp, apiKey, model, replyLanguage()))
            } catch (e: ScanException) {
                Ui.Failed(bmp, e.kind, e.message)
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) start(uri)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = cameraUri
        if (ok && uri != null) start(uri)
    }

    fun openCamera() {
        val dir = File(ctx.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("scan_", ".jpg", dir)
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", file)
        cameraUri = uri
        camera.launch(uri)
    }

    fun openGallery() {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold)
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (val s = ui) {
                is Ui.Idle -> HomeContent(
                    noKey = apiKey.isBlank(),
                    onCamera = ::openCamera,
                    onGallery = ::openGallery,
                    onAddKey = { showSettings = true },
                )
                is Ui.Scanning -> ScanningContent(s.bmp)
                is Ui.Done -> ResultContent(s.bmp, s.result, onAgain = { ui = Ui.Idle })
                is Ui.Failed -> ErrorContent(
                    kind = s.kind,
                    message = s.message,
                    canRetry = lastUri != null && s.bmp != null,
                    onRetry = { lastUri?.let { start(it) } },
                    onSettings = { showSettings = true },
                    onBack = { ui = Ui.Idle },
                )
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            initialKey = apiKey,
            initialModel = model,
            onDismiss = { showSettings = false },
            onSave = { k, m ->
                Prefs.save(ctx, k, m)
                apiKey = Prefs.key(ctx)
                model = Prefs.model(ctx)
                showSettings = false
            },
        )
    }
}

@Composable
private fun HomeContent(
    noKey: Boolean,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onAddKey: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Text(
                stringResource(R.string.home_lead),
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (noKey) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.no_key_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(stringResource(R.string.no_key_msg), style = MaterialTheme.typography.bodyMedium)
                    FilledTonalButton(onClick = onAddKey) { Text(stringResource(R.string.add_key)) }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onCamera,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.btn_camera))
            }
            FilledTonalButton(
                onClick = onGallery,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.btn_gallery))
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(R.string.tips_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                listOf(R.string.tip1, R.string.tip2, R.string.tip3, R.string.tip4).forEach { id ->
                    Row {
                        Text("\u2022  ")
                        Text(stringResource(id), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanningContent(bmp: Bitmap) {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (step < 4) {
            delay(1400)
            step++
        }
    }
    val steps = listOf(R.string.step1, R.string.step2, R.string.step3, R.string.step4, R.string.step5)
    val image = remember(bmp) { bmp.asImageBitmap() }

    val transition = rememberInfiniteTransition(label = "scan")
    val line by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "line",
    )
    val accent = MaterialTheme.colorScheme.primary

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            Canvas(Modifier.fillMaxSize()) {
                val y = size.height * line
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color.Transparent, accent, Color.Transparent)),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 6.dp.toPx(),
                )
            }
        }

        LinearProgressIndicator(
            progress = { (step + 1) / 5f },
            modifier = Modifier.fillMaxWidth(),
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            steps.forEachIndexed { i, label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        i < step -> Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                        i == step -> CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                        )
                        else -> Icon(
                            Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(label),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (i == step) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (i > step) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

private val GoodColor = Color(0xFF3D7A3A)
private val FairColor = Color(0xFFB07A12)
private val PoorColor = Color(0xFFA63A2A)

@Composable
private fun ScoreRing(score: Int?, color: Color) {
    var target by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(score) { target = (score ?: 0) / 100f }
    val progress by animateFloatAsState(targetValue = target, animationSpec = tween(900), label = "score")

    Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 10.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
        )
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 10.dp,
            color = color,
            trackColor = Color.Transparent,
        )
        Text(
            text = score?.toString() ?: "\u2013",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ResultContent(bmp: Bitmap, r: ScanResult, onAgain: () -> Unit) {
    val image = remember(bmp) { bmp.asImageBitmap() }
    val score = if (r.found) r.score else null
    val gradeRes = when {
        !r.found || score == null -> R.string.no_tobacco
        score >= 80 -> R.string.grade_good
        score >= 60 -> R.string.grade_fair
        else -> R.string.grade_poor
    }
    val gradeColor = when {
        !r.found || score == null -> MaterialTheme.colorScheme.outline
        score >= 80 -> GoodColor
        score >= 60 -> FairColor
        else -> PoorColor
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(28.dp)),
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ScoreRing(score, gradeColor)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(gradeRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = gradeColor,
                )
                if (r.what.isNotBlank()) {
                    Text(
                        r.what,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (r.found) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val rows = listOf(
                        R.string.row_form to r.form,
                        R.string.row_color to r.color,
                        R.string.row_curing to r.curing,
                        R.string.row_moisture to r.moisture,
                        R.string.row_confidence to "${r.confidence}%",
                    ).filter { it.second.isNotBlank() }
                    rows.forEachIndexed { i, (label, value) ->
                        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(stringResource(label), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        SectionCard(
            title = stringResource(R.string.defects),
            items = r.defects,
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer,
        )
        SectionCard(
            title = stringResource(R.string.positives),
            items = r.positives,
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        val advice = buildList {
            addAll(r.advice)
            if (!r.found || r.confidence < 60) {
                add(stringResource(R.string.low_conf))
                addAll(r.photoTips.take(2))
            }
        }
        SectionCard(
            title = stringResource(R.string.advice),
            items = advice,
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        Text(
            stringResource(R.string.limit),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        FilledTonalButton(onClick = onAgain, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(stringResource(R.string.scan_again))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionCard(title: String, items: List<String>, container: Color, content: Color) {
    if (items.isEmpty()) return
    Card(colors = CardDefaults.cardColors(containerColor = container, contentColor = content)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            items.forEach {
                Row {
                    Text("\u2022  ")
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    kind: FailKind,
    message: String?,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit,
) {
    val text = when (kind) {
        FailKind.INVALID_KEY -> stringResource(R.string.err_invalid_key)
        FailKind.RATE_LIMIT -> stringResource(R.string.err_rate)
        FailKind.NETWORK -> stringResource(R.string.err_network)
        FailKind.SERVER -> stringResource(R.string.err_server)
        FailKind.PARSE -> stringResource(R.string.err_parse)
        FailKind.OTHER -> stringResource(if (message == null) R.string.err_photo else R.string.err_other)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (kind == FailKind.OTHER && !message.isNullOrBlank()) {
                    Text(message, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (canRetry) {
                Button(onClick = onRetry, modifier = Modifier.weight(1f).height(52.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.retry))
                }
            }
            FilledTonalButton(
                onClick = if (kind == FailKind.INVALID_KEY) onSettings else onBack,
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Text(stringResource(if (kind == FailKind.INVALID_KEY) R.string.settings else R.string.scan_again))
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    initialKey: String,
    initialModel: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var key by remember { mutableStateOf(initialKey) }
    var model by remember { mutableStateOf(initialModel) }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text(stringResource(R.string.api_key)) },
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                            )
                        }
                    },
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text(stringResource(R.string.model)) },
                    singleLine = true,
                )
                Text(
                    stringResource(R.string.key_privacy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(key, model) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
