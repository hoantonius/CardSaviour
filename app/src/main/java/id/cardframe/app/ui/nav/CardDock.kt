package id.cardframe.app.ui.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import id.cardframe.app.ui.components.QrGlyph

enum class DockTab { Scan, Card, Qr, Settings }

@Composable
fun CardDock(
    tab: DockTab,
    cardEnabled: Boolean,
    onSelect: (DockTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            DockItem("Scan", Icons.Filled.Home, tab == DockTab.Scan, enabled = true) { onSelect(DockTab.Scan) }
            DockItem("Card", Icons.Filled.Edit, tab == DockTab.Card, enabled = cardEnabled) { onSelect(DockTab.Card) }
            DockItem("QR", icon = null, selected = tab == DockTab.Qr, enabled = cardEnabled) { onSelect(DockTab.Qr) }
            DockItem("Settings", Icons.Filled.Settings, tab == DockTab.Settings, enabled = true) { onSelect(DockTab.Settings) }
        }
    }
}

@Composable
private fun DockItem(
    label: String,
    icon: ImageVector?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val tint = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .heightIn(min = 56.dp)
            .alpha(if (enabled) 1f else 0.7f)
            .semantics {
                role = Role.Tab
                this.selected = selected
                contentDescription = label
            },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            } else {
                QrGlyph(tint = tint, modifier = Modifier.size(22.dp))
            }
            Text(label, style = MaterialTheme.typography.labelLarge, color = tint)
        }
    }
}
