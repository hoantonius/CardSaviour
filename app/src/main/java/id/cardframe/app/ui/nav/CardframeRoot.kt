package id.cardframe.app.ui.nav

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.cardframe.app.domain.theme.NightMode
import id.cardframe.app.ui.qr.QrScreen
import id.cardframe.app.ui.review.ReviewScreen
import id.cardframe.app.ui.scan.ScanScreen
import id.cardframe.app.ui.settings.SettingsScreen
import id.cardframe.app.ui.settings.SettingsViewModel
import id.cardframe.app.ui.theme.AuroraTheme

@Composable
fun CardframeRoot(settingsViewModel: SettingsViewModel = hiltViewModel()) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val dark = when (settings.night) {
        NightMode.System -> isSystemInDarkTheme()
        NightMode.Day -> false
        NightMode.Night -> true
    }
    AuroraTheme(theme = settings.theme, dark = dark) {
        val nav = rememberNavController()
        NavHost(navController = nav, startDestination = ScanRoute) {
            composable<ScanRoute> {
                ScanScreen(
                    autoCapture = settings.autoCapture,
                    onAutoCapture = settingsViewModel::setAutoCapture,
                    onOpenSettings = { nav.navigate(SettingsRoute) },
                    onOpenReview = {
                        nav.navigate(ReviewRoute) { launchSingleTop = true }
                    },
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
                    onTheme = settingsViewModel::setTheme,
                    onNight = settingsViewModel::setNight,
                    onAutoCapture = settingsViewModel::setAutoCapture,
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}
