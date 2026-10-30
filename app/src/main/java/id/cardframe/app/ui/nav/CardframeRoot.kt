package id.cardframe.app.ui.nav

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.cardframe.app.domain.theme.NightMode
import id.cardframe.app.ui.qr.QrScreen
import id.cardframe.app.ui.review.ReviewScreen
import id.cardframe.app.ui.scan.ScanScreen
import id.cardframe.app.ui.settings.SettingsScreen
import id.cardframe.app.ui.settings.SettingsViewModel
import id.cardframe.app.ui.theme.AuroraTheme

@Composable
fun CardframeRoot(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val hasCard by sessionViewModel.hasCard.collectAsStateWithLifecycle()
    val canUndo by sessionViewModel.canUndo.collectAsStateWithLifecycle()
    val dark = when (settings.night) {
        NightMode.System -> isSystemInDarkTheme()
        NightMode.Day -> false
        NightMode.Night -> true
    }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(canUndo) {
        if (!canUndo) return@LaunchedEffect
        val result = snackbar.showSnackbar(
            message = "Card discarded",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) sessionViewModel.undo() else sessionViewModel.dropUndo()
    }
    AuroraTheme(theme = settings.theme, dark = dark) {
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val tab = when {
            entry?.destination?.hasRoute<ReviewRoute>() == true -> DockTab.Card
            entry?.destination?.hasRoute<QrRoute>() == true -> DockTab.Qr
            entry?.destination?.hasRoute<SettingsRoute>() == true -> DockTab.Settings
            else -> DockTab.Scan
        }
        fun go(route: Any) {
            val action = {
                nav.navigate(route) {
                    popUpTo(ScanRoute) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            if (sessionViewModel.gate.onAttemptLeave?.invoke(action) == true) return
            action()
        }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                CardDock(
                    tab = tab,
                    cardEnabled = hasCard,
                    onSelect = { selected ->
                        when (selected) {
                            DockTab.Scan -> go(ScanRoute)
                            DockTab.Card -> if (hasCard) go(ReviewRoute)
                            DockTab.Qr -> if (hasCard) go(QrRoute)
                            DockTab.Settings -> go(SettingsRoute)
                        }
                    },
                )
            },
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = ScanRoute,
                modifier = androidx.compose.ui.Modifier.padding(padding),
            ) {
                composable<ScanRoute> {
                    ScanScreen(
                        autoCapture = settings.autoCapture,
                        onAutoCapture = settingsViewModel::setAutoCapture,
                        onOpenReview = { nav.navigate(ReviewRoute) { launchSingleTop = true } },
                        whatsNew = settingsViewModel.latest.takeIf { settings.seenRelease != it.version },
                        onDismissWhatsNew = settingsViewModel::dismissWhatsNew,
                    )
                }
                composable<ReviewRoute> {
                    ReviewScreen(
                        onBack = { nav.popBackStack() },
                        onQr = { nav.navigate(QrRoute) },
                    )
                }
                composable<QrRoute> {
                    QrScreen(onBack = { nav.popBackStack() })
                }
                composable<SettingsRoute> {
                    SettingsScreen(
                        settings = settings,
                        releases = settingsViewModel.releases,
                        onTheme = settingsViewModel::setTheme,
                        onNight = settingsViewModel::setNight,
                        onAutoCapture = settingsViewModel::setAutoCapture,
                        onBack = { nav.popBackStack() },
                    )
                }
            }
        }
    }
}
