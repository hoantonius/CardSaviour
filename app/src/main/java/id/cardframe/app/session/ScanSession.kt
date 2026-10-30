package id.cardframe.app.session

import android.graphics.Bitmap
import id.cardframe.app.domain.model.ContactDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory only. Discard removes it, with one undo. Nothing is written to disk. */
@Singleton
class ScanSession @Inject constructor() {
    private val draftState = MutableStateFlow<ContactDraft?>(null)
    private val previewState = MutableStateFlow<Bitmap?>(null)
    private val undoState = MutableStateFlow<HeldCard?>(null)

    val draft: StateFlow<ContactDraft?> = draftState.asStateFlow()
    val preview: StateFlow<Bitmap?> = previewState.asStateFlow()
    val undo: StateFlow<HeldCard?> = undoState.asStateFlow()

    fun publish(draft: ContactDraft, preview: Bitmap?) {
        undoState.value = null
        draftState.value = draft
        previewState.value = preview
    }

    fun update(draft: ContactDraft) {
        draftState.value = draft
    }

    fun clear() {
        draftState.value = null
        previewState.value = null
    }

    /** Drops the in-memory card and keeps it only long enough to undo. */
    fun stashUndo() {
        val held = draftState.value ?: return
        undoState.value = HeldCard(held, previewState.value)
        clear()
    }

    fun restoreUndo() {
        val held = undoState.value ?: return
        draftState.value = held.draft
        previewState.value = held.preview
        undoState.value = null
    }

    fun dropUndo() {
        undoState.value = null
    }
}

data class HeldCard(
    val draft: ContactDraft,
    val preview: Bitmap?,
)
