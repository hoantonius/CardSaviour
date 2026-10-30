package id.cardframe.app.ui.scan

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.cardframe.app.data.image.BitmapDecoder
import id.cardframe.app.data.ocr.CardTextReader
import id.cardframe.app.domain.model.CaptureProblem
import id.cardframe.app.domain.image.LumaStats
import id.cardframe.app.domain.parse.CardParser
import id.cardframe.app.session.ScanSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

data class ScanUiState(
    val stage: Stage = Stage.Camera,
    val failure: CaptureProblem? = null,
    val canForce: Boolean = false,
) {
    enum class Stage { Camera, Reading, Failed }
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val reader: CardTextReader,
    private val parser: CardParser,
    private val decoder: BitmapDecoder,
    private val session: ScanSession,
) : ViewModel() {
    private val ui = MutableStateFlow(ScanUiState())
    val state = ui.asStateFlow()

    private val effectFlow = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    val effects = effectFlow.asSharedFlow()

    private var pending: Bitmap? = null

    fun onPhoto(uri: Uri) {
        viewModelScope.launch {
            ui.update { it.copy(stage = ScanUiState.Stage.Reading, failure = null, canForce = false) }
            val bitmap = try {
                withContext(Dispatchers.IO) { decoder.decode(uri) }
            } catch (_: Exception) {
                ui.update { it.copy(stage = ScanUiState.Stage.Failed, failure = CaptureProblem.Unreadable) }
                return@launch
            }
            submit(bitmap, checkQuality = true)
        }
    }

    fun onCapture(bitmap: Bitmap) {
        viewModelScope.launch { submit(bitmap, checkQuality = true) }
    }

    fun forceRead() {
        val bitmap = pending ?: return
        viewModelScope.launch { submit(bitmap, checkQuality = false) }
    }

    fun retake() {
        pending = null
        ui.update { ScanUiState() }
    }

    private suspend fun submit(bitmap: Bitmap, checkQuality: Boolean) {
        ui.update { it.copy(stage = ScanUiState.Stage.Reading, failure = null, canForce = false) }
        val framed = withContext(Dispatchers.Default) { CardFrame.crop(bitmap) }
        pending = framed
        if (checkQuality) {
            val problem = withContext(Dispatchers.Default) { quality(framed) }
            if (problem != null) {
                ui.update {
                    it.copy(stage = ScanUiState.Stage.Failed, failure = problem, canForce = true)
                }
                return
            }
        }
        val text = try {
            withTimeout(20_000) { reader.read(framed) }
        } catch (_: TimeoutCancellationException) {
            fail(CaptureProblem.Unreadable, canForce = false)
            return
        } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            fail(CaptureProblem.Unreadable, canForce = false)
            return
        }
        if (text.trim().length < 3) {
            fail(CaptureProblem.NoText, canForce = false)
            return
        }
        val draft = parser.parse(text)
        if (!draft.hasSignal()) {
            fail(CaptureProblem.NoText, canForce = false)
            return
        }
        session.publish(draft, framed)
        pending = null
        ui.update { ScanUiState() }
        effectFlow.emit(Effect.OpenReview)
    }

    private fun fail(problem: CaptureProblem, canForce: Boolean) {
        ui.update { it.copy(stage = ScanUiState.Stage.Failed, failure = problem, canForce = canForce) }
    }

    private fun quality(bitmap: Bitmap): CaptureProblem? {
        val width = 96
        val height = (width * bitmap.height / bitmap.width.toFloat()).toInt().coerceAtLeast(3)
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)
        val luma = IntArray(pixels.size) { index ->
            val color = pixels[index]
            val red = (color shr 16) and 0xFF
            val green = (color shr 8) and 0xFF
            val blue = color and 0xFF
            (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
        }
        return LumaStats.judge(LumaStats.mean(luma), LumaStats.laplacianVariance(luma, width, height))
    }

    sealed interface Effect {
        data object OpenReview : Effect
    }
}
