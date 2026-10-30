package id.cardframe.app.ui.review

import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import id.cardframe.app.data.contacts.ContactIntents
import id.cardframe.app.domain.model.ContactDraft
import id.cardframe.app.domain.vcard.VCardWriter
import id.cardframe.app.session.ScanSession
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val session: ScanSession,
) : ViewModel() {
    val draft: StateFlow<ContactDraft?> = session.draft
    val preview = session.preview

    fun edit(block: (ContactDraft) -> ContactDraft) {
        val current = session.draft.value ?: return
        session.update(block(current))
    }

    fun vcard(): String? = session.draft.value?.let(VCardWriter::build)

    fun fileName(): String = session.draft.value?.let(VCardWriter::fileName) ?: "contact.vcf"

    fun insertIntent(): Intent? = session.draft.value?.let(ContactIntents::insert)

    fun clear() = session.clear()
}
