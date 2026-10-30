package id.cardframe.app.domain.qr

import org.junit.Assert.assertTrue
import org.junit.Test

class QrColorsTest {
    @Test
    fun modulesStayReadableOnAWhiteQuietZone() {
        assertTrue(QrColors.moduleContrast() >= 7.0)
        assertTrue(QrColors.DARK < QrColors.LIGHT)
    }
}
