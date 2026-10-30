package id.cardframe.app.domain.image

/**
 * Fires once when several sharp, evenly lit frames arrive in a row.
 * A dark, blown-out, or blurry frame resets the streak.
 */
class SteadyCardGate(
    private val framesNeeded: Int = 6,
    private val maxLumaDrift: Double = 8.0,
) {
    private var streak = 0
    private var lastMean = -1.0
    private var latched = false

    fun reset() {
        streak = 0
        lastMean = -1.0
        latched = false
    }

    fun observe(meanLuma: Double, acceptable: Boolean): Boolean {
        if (latched) return false
        val stable = lastMean < 0 || kotlin.math.abs(meanLuma - lastMean) <= maxLumaDrift
        lastMean = meanLuma
        if (acceptable && stable) {
            streak += 1
        } else {
            streak = 0
        }
        if (streak >= framesNeeded) {
            latched = true
            return true
        }
        return false
    }
}
