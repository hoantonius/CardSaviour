package id.cardframe.app.session

import android.graphics.Bitmap
import id.cardframe.app.domain.model.ContactDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory only. Cleared when the user leaves the card. Nothing is written to disk. */
@Singleton
class ScanSession @Inject constructor() {
    private val draftState = MutableStateFlow<ContactDraft?>(null)
    private val previewState = MutableStateFlow<Bitmap?>(null)

    val draft: StateFlow<ContactDraft?> = draftState.asStateFlow()
    val preview: StateFlow<Bitmap?> = previewState.asStateFlow()

    fun publish(draft: ContactDraft, preview: Bitmap?) {
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
}
