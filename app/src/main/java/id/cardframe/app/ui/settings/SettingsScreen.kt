package id.cardframe.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import id.cardframe.app.data.settings.AppSettings
import id.cardframe.app.domain.theme.AuroraThemeEngine
import id.cardframe.app.domain.theme.AuroraThemeId
import id.cardframe.app.domain.theme.NightMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onTheme: (AuroraThemeId) -> Unit,
    onNight: (NightMode) -> Unit,
    onAutoCapture: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val nights = listOf(NightMode.Day, NightMode.System, NightMode.Night)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Text(
                "Four themes, each in day and night.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            AuroraThemeEngine.ids.forEach { id ->
                val selected = settings.theme == id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .padding(bottom = 8.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .selectable(selected = selected, onClick = { onTheme(id) }, role = Role.RadioButton)
                        .padding(horizontal = 12.dp),
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
            Text("Appearance", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                nights.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = settings.night == mode,
                        onClick = { onNight(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = nights.size),
                    ) {
                        Text(
                            when (mode) {
                                NightMode.Day -> "Day"
                                NightMode.System -> "System"
                                NightMode.Night -> "Night"
                            },
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Steady capture", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Take the photo when the card is sharp and well lit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = settings.autoCapture, onCheckedChange = onAutoCapture)
            }
            Text(
                "Scans are not saved in the app. Saving opens the phone’s Add Contact screen. The phone book syncs with the account already on the device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 20.dp, bottom = 24.dp),
            )
        }
    }
}
