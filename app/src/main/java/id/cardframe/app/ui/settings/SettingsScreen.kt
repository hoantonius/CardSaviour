package id.cardframe.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import id.cardframe.app.data.settings.AppSettings
import id.cardframe.app.domain.release.ReleaseNote
import id.cardframe.app.domain.theme.AuroraThemeEngine
import id.cardframe.app.domain.theme.AuroraThemeId
import id.cardframe.app.domain.theme.NightMode

private enum class SettingsSheet { Theme, Night, History }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    releases: List<ReleaseNote>,
    onTheme: (AuroraThemeId) -> Unit,
    onNight: (NightMode) -> Unit,
    onAutoCapture: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var sheet by rememberSaveable { mutableStateOf<SettingsSheet?>(null) }
    val needle = query.trim()
    fun show(vararg terms: String) = needle.isEmpty() || terms.any { it.contains(needle, ignoreCase = true) }
    val themeLabel = AuroraThemeEngine.label(settings.theme)
    val nightLabel = nightLabel(settings.night)
    val captureLabel = if (settings.autoCapture) "On" else "Off"
    val version = releases.firstOrNull()?.version ?: "1.1.0"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                label = { Text("Search settings") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
            if (show("appearance", "theme", themeLabel, "ink", "harbor", "copper", "grove")) {
                SectionLabel("Appearance")
                SettingLink(
                    icon = Icons.Filled.Settings,
                    title = "Theme",
                    value = themeLabel,
                    onClick = { sheet = SettingsSheet.Theme },
                )
            }
            if (show("appearance", "night", "day", "system", nightLabel)) {
                SettingLink(
                    icon = Icons.Filled.Settings,
                    title = "Day or night",
                    value = nightLabel,
                    onClick = { sheet = SettingsSheet.Night },
                )
            }
            if (show("capture", "steady", "auto", captureLabel, "camera")) {
                SectionLabel("Capture")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Steady capture", style = MaterialTheme.typography.titleMedium)
                        Text(
                            captureLabel + " · take the photo when the card is sharp and well lit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = settings.autoCapture, onCheckedChange = onAutoCapture)
                }
            }
            if (show("about", "version", "history", "what", version, "changelog")) {
                SectionLabel("About")
                SettingLink(
                    icon = Icons.Filled.Info,
                    title = "Version history",
                    value = version,
                    onClick = { sheet = SettingsSheet.History },
                )
                Text(
                    "Scans are not saved in the app. Saving opens the phone’s Add Contact screen. The phone book syncs with the account already on the device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                )
            }
        }
    }

    val open = sheet
    if (open != null) {
        ModalBottomSheet(
            onDismissRequest = { sheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                when (open) {
                    SettingsSheet.Theme -> {
                        Text("Theme", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Four presets. Each one stays readable in day and night.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        AuroraThemeEngine.ids.forEach { id ->
                            val selected = settings.theme == id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 64.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .selectable(selected = selected, onClick = { onTheme(id) }, role = Role.RadioButton),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected, onClick = null)
                                Column(Modifier.padding(start = 8.dp)) {
                                    Text(AuroraThemeEngine.label(id), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        AuroraThemeEngine.caption(id),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                    SettingsSheet.Night -> {
                        Text("Day or night", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))
                        listOf(NightMode.Day, NightMode.System, NightMode.Night).forEach { mode ->
                            val selected = settings.night == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .selectable(selected = selected, onClick = { onNight(mode) }, role = Role.RadioButton),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected, onClick = null)
                                Text(nightLabel(mode), modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    SettingsSheet.History -> {
                        Text("Version history", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                        releases.forEach { release ->
                            Text(release.version, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                            Text(release.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            release.items.forEach { item ->
                                Text(
                                    "${item.kind} · ${item.text}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingLink(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

private fun nightLabel(mode: NightMode): String = when (mode) {
    NightMode.Day -> "Day"
    NightMode.System -> "Match the phone"
    NightMode.Night -> "Night"
}
