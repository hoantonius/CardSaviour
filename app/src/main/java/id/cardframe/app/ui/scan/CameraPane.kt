package id.cardframe.app.ui.scan

import android.graphics.Bitmap
import android.util.Rational
import android.view.Surface
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.cardframe.app.data.image.FrameLuma
import id.cardframe.app.data.image.ImageProxyBitmap
import id.cardframe.app.domain.image.LumaStats
import id.cardframe.app.domain.image.SteadyCardGate
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun CameraPane(
    active: Boolean,
    autoCapture: Boolean,
    torchOn: Boolean,
    onBitmap: (Bitmap) -> Unit,
    onUnavailable: () -> Unit,
    onFlashAvailable: (Boolean) -> Unit,
    onShutter: (((() -> Unit)) -> Unit),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val onBitmapState = rememberUpdatedState(onBitmap)
    val onUnavailableState = rememberUpdatedState(onUnavailable)
    val onFlashState = rememberUpdatedState(onFlashAvailable)
    val onShutterState = rememberUpdatedState(onShutter)
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    DisposableEffect(lifecycleOwner, active, autoCapture) {
        val future = ProcessCameraProvider.getInstance(context)
        val busy = AtomicBoolean(false)
        val gate = SteadyCardGate()
        val listener = Runnable {
            val provider = try {
                future.get()
            } catch (_: Exception) {
                onUnavailableState.value()
                return@Runnable
            }
            if (!active) {
                provider.unbindAll()
                cameraControl = null
                onShutterState.value { }
                return@Runnable
            }
            val selector = when {
                provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                else -> {
                    onUnavailableState.value()
                    return@Runnable
                }
            }
            val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
            val preview = Preview.Builder().setTargetRotation(rotation).build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetRotation(rotation)
                .build()
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(rotation)
                .build()
            if (autoCapture) {
                analysis.setAnalyzer(analysisExecutor) { image ->
                    try {
                        val sample = FrameLuma.sample(image)
                        val acceptable = LumaStats.judge(sample.mean, sample.variance) == null
                        if (gate.observe(sample.mean, acceptable)) {
                            mainExecutor.execute {
                                takePhoto(capture, mainExecutor, busy, onBitmapState.value)
                            }
                        }
                    } finally {
                        image.close()
                    }
                }
            }
            try {
                provider.unbindAll()
                val width = previewView.width.coerceAtLeast(1)
                val height = previewView.height.coerceAtLeast(1)
                val group = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(capture)
                    .apply { if (autoCapture) addUseCase(analysis) }
                    .setViewPort(ViewPort.Builder(Rational(width, height), rotation).build())
                    .build()
                val camera = provider.bindToLifecycle(lifecycleOwner, selector, group)
                cameraControl = camera.cameraControl
                val flash = camera.cameraInfo.hasFlashUnit()
                onFlashState.value(flash)
                onShutterState.value {
                    takePhoto(capture, mainExecutor, busy, onBitmapState.value)
                }
            } catch (_: Exception) {
                onUnavailableState.value()
            }
        }
        future.addListener(listener, mainExecutor)
        onDispose {
            cameraControl = null
            runCatching { if (future.isDone) future.get().unbindAll() }
        }
    }

    LaunchedEffect(torchOn, cameraControl) {
        cameraControl?.enableTorch(torchOn)
    }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

private fun takePhoto(
    capture: ImageCapture,
    executor: Executor,
    busy: AtomicBoolean,
    onBitmap: (Bitmap) -> Unit,
) {
    if (!busy.compareAndSet(false, true)) return
    capture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = runCatching { ImageProxyBitmap.decode(image) }.getOrNull()
                image.close()
                busy.set(false)
                if (bitmap != null) onBitmap(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                busy.set(false)
            }
        },
    )
}
