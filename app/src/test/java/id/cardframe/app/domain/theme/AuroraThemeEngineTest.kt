package id.cardframe.app.domain.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuroraThemeEngineTest {
    @Test
    fun fourThemesTimesDayAndNightStayReadable() {
        assertEquals(4, AuroraThemeEngine.ids.size)
        AuroraThemeEngine.ids.forEach { id ->
            listOf(false, true).forEach { dark ->
                val palette = AuroraThemeEngine.palette(id, dark)
                assertTrue(
                    "$id dark=$dark ink/background",
                    AuroraThemeEngine.contrastRatio(palette.ink, palette.background) >= 4.5,
                )
                assertTrue(
                    "$id dark=$dark onPrimary/primary",
                    AuroraThemeEngine.contrastRatio(palette.onPrimary, palette.primary) >= 4.5,
                )
                assertTrue(
                    "$id dark=$dark muted/surface",
                    AuroraThemeEngine.contrastRatio(palette.muted, palette.surface) >= 4.5,
                )
            }
        }
    }
}
