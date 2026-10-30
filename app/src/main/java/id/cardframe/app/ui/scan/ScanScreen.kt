package id.cardframe.app.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.cardframe.app.R
import id.cardframe.app.domain.model.CaptureProblem
import id.cardframe.app.domain.release.ReleaseNote
import id.cardframe.app.ui.theme.auroraPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    autoCapture: Boolean,
    onAutoCapture: (Boolean) -> Unit,
    onOpenReview: () -> Unit,
    whatsNew: ReleaseNote? = null,
    onDismissWhatsNew: () -> Unit = {},
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var unavailable by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    var shutter by remember { mutableStateOf<() -> Unit>({ }) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { allowed -> granted = allowed }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhoto(uri)
    }

    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is ScanViewModel.Effect.OpenReview) onOpenReview()
        }
    }

    val palette = auroraPalette()
    val reading = state.stage == ScanUiState.Stage.Reading
    val cameraOn = granted && !unavailable && state.stage == ScanUiState.Stage.Camera

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp)
                    .background(Color(palette.surfaceHigh), MaterialTheme.shapes.large)
                    .semantics { contentDescription = "Camera preview. Place the card inside the frame." },
            ) {
                if (granted && !unavailable) {
                    CameraPane(
                        active = cameraOn,
                        autoCapture = autoCapture && cameraOn,
                        torchOn = torchOn,
                        onBitmap = viewModel::onCapture,
                        onUnavailable = { unavailable = true },
                        onFlashAvailable = { hasFlash = it },
                        onShutter = { shutter = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                    CardGuide(modifier = Modifier.fillMaxSize())
                }
                if (reading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.86f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Reading the card…", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Text(
                text = when {
                    !granted -> "Allow the camera, or choose a photo of the card."
                    unavailable -> "This device has no camera. Choose a photo instead."
                    state.failure != null -> failureMessage(state.failure!!)
                    else -> "Place the card inside the frame."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(16.dp))
            whatsNew?.let { note ->
                WhatsNewCard(note, onDismissWhatsNew)
                Spacer(Modifier.height(12.dp))
            }
            Button(
                onClick = shutter,
                enabled = cameraOn && !reading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Text("Scan card")
            }
            if (state.canForce && state.failure != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = viewModel::forceRead,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    Text("Read it anyway")
                }
            }
            if (state.failure != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = viewModel::retake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    Text("Try again")
                }
            }
            TextButton(
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                enabled = !reading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Choose a photo")
            }
            if (!granted) {
                TextButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    Text("Allow camera")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Capture when the card is steady", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = autoCapture,
                    onCheckedChange = onAutoCapture,
                    enabled = granted && !unavailable,
                )
            }
            if (hasFlash && granted) {
                TextButton(
                    onClick = { torchOn = !torchOn },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(if (torchOn) "Turn flash off" else "Turn flash on")
                }
            }
            Text(
                text = stringResource(R.string.privacy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun WhatsNewCard(note: ReleaseNote, onDismiss: () -> Unit) {
    val palette = auroraPalette()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(palette.surface), MaterialTheme.shapes.large)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "What's new in ${note.version}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss what's new")
            }
        }
        note.items.take(3).forEach { item ->
            val color = when (item.kind) {
                "New" -> Color(palette.success)
                "Fixed" -> Color(palette.caution)
                else -> MaterialTheme.colorScheme.primary
            }
            Text(
                "${item.kind} · ${item.text}",
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun CardGuide(modifier: Modifier = Modifier) {
    val palette = auroraPalette()
    val ink = Color(palette.ink)
    val bracket = Color(palette.primary)
    Canvas(modifier) {
        val frame = CardFrame.rect(size.width, size.height)
        val cutout = Path().apply {
            addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
            addRoundRect(
                RoundRect(
                    left = frame.left,
                    top = frame.top,
                    right = frame.right,
                    bottom = frame.bottom,
                    cornerRadius = CornerRadius(28f, 28f),
                ),
            )
            fillType = PathFillType.EvenOdd
        }
        drawPath(cutout, ink.copy(alpha = 0.45f))
        val arm = 28.dp.toPx()
        val stroke = Stroke(width = 4.dp.toPx())
        val corners = listOf(
            Offset(frame.left, frame.top) to listOf(Offset(arm, 0f), Offset(0f, arm)),
            Offset(frame.right, frame.top) to listOf(Offset(-arm, 0f), Offset(0f, arm)),
            Offset(frame.left, frame.bottom) to listOf(Offset(arm, 0f), Offset(0f, -arm)),
            Offset(frame.right, frame.bottom) to listOf(Offset(-arm, 0f), Offset(0f, -arm)),
        )
        corners.forEach { (origin, arms) ->
            arms.forEach { delta ->
                drawLine(bracket, origin, origin + delta, strokeWidth = stroke.width)
            }
        }
    }
}

private fun failureMessage(problem: CaptureProblem): String = when (problem) {
    CaptureProblem.Dark -> "The card is too dark. Move to better light, or turn the flash on."
    CaptureProblem.Bright -> "The card is washed out. Tilt it so the light is not reflecting."
    CaptureProblem.Blurry -> "The photo is blurry. Hold the phone steady and fill the frame."
    CaptureProblem.NoText -> "No contact details could be read. Use a sharper photo with the card filling the frame."
    CaptureProblem.Unreadable -> "The photo could not be read. Try again."
}
