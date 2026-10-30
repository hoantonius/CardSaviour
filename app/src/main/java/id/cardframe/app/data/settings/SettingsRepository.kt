package id.cardframe.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import id.cardframe.app.domain.theme.AuroraThemeId
import id.cardframe.app.domain.theme.NightMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class AppSettings(
    val theme: AuroraThemeId = AuroraThemeId.Ink,
    val night: NightMode = NightMode.System,
    val autoCapture: Boolean = true,
    val seenRelease: String = "",
)

private val Context.settingsStore by preferencesDataStore(name = "cardframe_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val settings: Flow<AppSettings> = context.settingsStore.data
        .map { prefs -> prefs.toSettings() }
        .distinctUntilChanged()

    suspend fun setTheme(theme: AuroraThemeId) {
        context.settingsStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setNight(night: NightMode) {
        context.settingsStore.edit { it[Keys.NIGHT] = night.name }
    }

    suspend fun setAutoCapture(enabled: Boolean) {
        context.settingsStore.edit { it[Keys.AUTO] = enabled }
    }

    suspend fun setSeenRelease(version: String) {
        context.settingsStore.edit { it[Keys.SEEN] = version }
    }

    private fun Preferences.toSettings(): AppSettings = AppSettings(
        theme = this[Keys.THEME].toEnum(AuroraThemeId.Ink),
        night = this[Keys.NIGHT].toEnum(NightMode.System),
        autoCapture = this[Keys.AUTO] ?: true,
        seenRelease = this[Keys.SEEN].orEmpty(),
    )

    private inline fun <reified T : Enum<T>> String?.toEnum(fallback: T): T =
        enumValues<T>().firstOrNull { it.name == this } ?: fallback

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val NIGHT = stringPreferencesKey("night")
        val AUTO = booleanPreferencesKey("auto_capture")
        val SEEN = stringPreferencesKey("seen_release")
    }
}
