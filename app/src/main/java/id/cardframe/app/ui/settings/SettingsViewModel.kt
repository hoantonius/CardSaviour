package id.cardframe.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.cardframe.app.data.release.ChangelogRepository
import id.cardframe.app.data.settings.AppSettings
import id.cardframe.app.data.settings.SettingsRepository
import id.cardframe.app.domain.release.ChangelogParser
import id.cardframe.app.domain.release.ReleaseNote
import id.cardframe.app.domain.theme.AuroraThemeId
import id.cardframe.app.domain.theme.NightMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    changelog: ChangelogRepository,
) : ViewModel() {
    val releases: List<ReleaseNote> = changelog.file.releases
    val latest: ReleaseNote = ChangelogParser.latest(changelog.file)

    val settings = repository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppSettings(),
    )

    fun setTheme(theme: AuroraThemeId) {
        viewModelScope.launch { repository.setTheme(theme) }
    }

    fun setNight(night: NightMode) {
        viewModelScope.launch { repository.setNight(night) }
    }

    fun setAutoCapture(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoCapture(enabled) }
    }

    fun dismissWhatsNew() {
        viewModelScope.launch { repository.setSeenRelease(latest.version) }
    }
}
