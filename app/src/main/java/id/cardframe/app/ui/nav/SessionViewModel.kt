package id.cardframe.app.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.cardframe.app.session.ReviewGate
import id.cardframe.app.session.ScanSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val session: ScanSession,
    val gate: ReviewGate,
) : ViewModel() {
    val hasCard = session.draft
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val canUndo = session.undo
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun undo() = session.restoreUndo()

    fun dropUndo() = session.dropUndo()
}
