package id.cardframe.app.domain.qr

import id.cardframe.app.domain.theme.AuroraThemeEngine

/** Machine-readable colors. Independent of the active theme. */
object QrColors {
    const val DARK = 0xFF000000L
    const val LIGHT = 0xFFFFFFFFL

    fun moduleContrast(): Double = AuroraThemeEngine.contrastRatio(DARK, LIGHT)
}
