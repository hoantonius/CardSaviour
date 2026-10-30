package id.cardframe.app.domain.theme

enum class AuroraThemeId {
    Ink,
    Harbor,
    Copper,
    Grove,
}

enum class NightMode {
    System,
    Day,
    Night,
}

data class AuroraPalette(
    val background: Long,
    val surface: Long,
    val surfaceHigh: Long,
    val ink: Long,
    val muted: Long,
    val line: Long,
    val primary: Long,
    val onPrimary: Long,
    val accent: Long,
    val onAccent: Long,
    val caution: Long,
    val onCaution: Long,
    val danger: Long,
    val success: Long,
)

object AuroraThemeEngine {
    val ids: List<AuroraThemeId> = AuroraThemeId.entries

    fun label(id: AuroraThemeId): String = when (id) {
        AuroraThemeId.Ink -> "Ink"
        AuroraThemeId.Harbor -> "Harbor"
        AuroraThemeId.Copper -> "Copper"
        AuroraThemeId.Grove -> "Grove"
    }

    fun caption(id: AuroraThemeId): String = when (id) {
        AuroraThemeId.Ink -> "Paper and charcoal"
        AuroraThemeId.Harbor -> "Sea and sand"
        AuroraThemeId.Copper -> "Bronze and cream"
        AuroraThemeId.Grove -> "Forest and stone"
    }

    fun palette(id: AuroraThemeId, dark: Boolean): AuroraPalette = when (id) {
        AuroraThemeId.Ink -> if (dark) inkNight else inkDay
        AuroraThemeId.Harbor -> if (dark) harborNight else harborDay
        AuroraThemeId.Copper -> if (dark) copperNight else copperDay
        AuroraThemeId.Grove -> if (dark) groveNight else groveDay
    }

    fun contrastRatio(foreground: Long, background: Long): Double {
        val lighter = maxOf(luminance(foreground), luminance(background))
        val darker = minOf(luminance(foreground), luminance(background))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun luminance(argb: Long): Double {
        fun channel(value: Double) =
            if (value <= 0.04045) value / 12.92 else Math.pow((value + 0.055) / 1.055, 2.4)
        val red = ((argb shr 16) and 0xFF) / 255.0
        val green = ((argb shr 8) and 0xFF) / 255.0
        val blue = (argb and 0xFF) / 255.0
        return 0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)
    }

    private val inkDay = palette(
        background = 0xFFF4F0E6, surface = 0xFFFFFDF8, surfaceHigh = 0xFFE9E2D4,
        ink = 0xFF1C1915, muted = 0xFF5C564C, line = 0xFFD9D0C2,
        primary = 0xFF1F3A34, onPrimary = 0xFFF4F0E6, accent = 0xFF8C5A2A, onAccent = 0xFFFFFDF8,
        caution = 0xFF8A4B08, onCaution = 0xFFFFF8EE, danger = 0xFF8E2F2F, success = 0xFF0E6B3A,
    )
    private val inkNight = palette(
        background = 0xFF121410, surface = 0xFF1C1F1A, surfaceHigh = 0xFF262A24,
        ink = 0xFFF3EDE2, muted = 0xFFC8C0B2, line = 0xFF3A4038,
        primary = 0xFFD7C4A3, onPrimary = 0xFF1C160F, accent = 0xFFE0A36A, onAccent = 0xFF1C140E,
        caution = 0xFFE6B15A, onCaution = 0xFF1C140C, danger = 0xFFFF8B7A, success = 0xFF7DCEA0,
    )
    private val harborDay = palette(
        background = 0xFFE7EEF2, surface = 0xFFF7FBFC, surfaceHigh = 0xFFD7E4EA,
        ink = 0xFF102027, muted = 0xFF3E5560, line = 0xFFC5D3DA,
        primary = 0xFF0E3A4D, onPrimary = 0xFFF7FBFC, accent = 0xFFC46B4A, onAccent = 0xFFFFF9F6,
        caution = 0xFF8A4B08, onCaution = 0xFFFFF8EE, danger = 0xFF8E2F2F, success = 0xFF0E6B3A,
    )
    private val harborNight = palette(
        background = 0xFF0C1418, surface = 0xFF152026, surfaceHigh = 0xFF1E2C34,
        ink = 0xFFE5F1F6, muted = 0xFFB7C9D2, line = 0xFF2E4550,
        primary = 0xFF8FCBE0, onPrimary = 0xFF062028, accent = 0xFFE7A08A, onAccent = 0xFF2A120C,
        caution = 0xFFE6B15A, onCaution = 0xFF1C140C, danger = 0xFFFF8B7A, success = 0xFF7DCEA0,
    )
    private val copperDay = palette(
        background = 0xFFF6EFE6, surface = 0xFFFFF9F3, surfaceHigh = 0xFFF0E2D4,
        ink = 0xFF24170F, muted = 0xFF6A5142, line = 0xFFE4D3C4,
        primary = 0xFF7A3E12, onPrimary = 0xFFFFF9F3, accent = 0xFF2C4A3E, onAccent = 0xFFF4FBF7,
        caution = 0xFF8A4B08, onCaution = 0xFFFFF8EE, danger = 0xFF8E2F2F, success = 0xFF0E6B3A,
    )
    private val copperNight = palette(
        background = 0xFF16110E, surface = 0xFF241C16, surfaceHigh = 0xFF31261D,
        ink = 0xFFF8EDE3, muted = 0xFFD2C0B0, line = 0xFF4A3B30,
        primary = 0xFFE2A36A, onPrimary = 0xFF26150A, accent = 0xFF9DCAAA, onAccent = 0xFF0E1C14,
        caution = 0xFFE6B15A, onCaution = 0xFF1C140C, danger = 0xFFFF8B7A, success = 0xFF7DCEA0,
    )
    private val groveDay = palette(
        background = 0xFFE8F0E6, surface = 0xFFF6FAF4, surfaceHigh = 0xFFD7E6D6,
        ink = 0xFF132117, muted = 0xFF3E5346, line = 0xFFC5D5C8,
        primary = 0xFF1E4D32, onPrimary = 0xFFF6FAF4, accent = 0xFFA3542F, onAccent = 0xFFFFF8F4,
        caution = 0xFF8A4B08, onCaution = 0xFFFFF8EE, danger = 0xFF8E2F2F, success = 0xFF0E6B3A,
    )
    private val groveNight = palette(
        background = 0xFF101611, surface = 0xFF1A241C, surfaceHigh = 0xFF243028,
        ink = 0xFFE7F2EA, muted = 0xFFB7C9BC, line = 0xFF314238,
        primary = 0xFF9DCAAA, onPrimary = 0xFF0E1C14, accent = 0xFFE0A36A, onAccent = 0xFF26150A,
        caution = 0xFFE6B15A, onCaution = 0xFF1C140C, danger = 0xFFFF8B7A, success = 0xFF7DCEA0,
    )

    private fun palette(
        background: Long,
        surface: Long,
        surfaceHigh: Long,
        ink: Long,
        muted: Long,
        line: Long,
        primary: Long,
        onPrimary: Long,
        accent: Long,
        onAccent: Long,
        caution: Long,
        onCaution: Long,
        danger: Long,
        success: Long,
    ) = AuroraPalette(
        background, surface, surfaceHigh, ink, muted, line,
        primary, onPrimary, accent, onAccent, caution, onCaution, danger, success,
    )
}
