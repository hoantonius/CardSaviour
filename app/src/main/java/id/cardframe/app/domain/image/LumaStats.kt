package id.cardframe.app.domain.image

import id.cardframe.app.domain.model.CaptureProblem

object LumaStats {
    const val DARK_BELOW = 42.0
    const val BRIGHT_ABOVE = 232.0
    const val BLUR_BELOW = 70.0

    fun mean(luma: IntArray, length: Int = luma.size): Double {
        if (length <= 0) return 0.0
        var sum = 0.0
        for (i in 0 until length) sum += luma[i]
        return sum / length
    }

    fun laplacianVariance(luma: IntArray, width: Int, height: Int): Double {
        if (width < 3 || height < 3 || luma.size < width * height) return 0.0
        var sum = 0.0
        var sumSq = 0.0
        var count = 0
        for (y in 1 until height - 1) {
            val row = y * width
            for (x in 1 until width - 1) {
                val center = luma[row + x]
                val lap = (
                    4 * center -
                        luma[row + x - 1] -
                        luma[row + x + 1] -
                        luma[row - width + x] -
                        luma[row + width + x]
                    ).toDouble()
                sum += lap
                sumSq += lap * lap
                count++
            }
        }
        if (count == 0) return 0.0
        val average = sum / count
        return (sumSq / count) - average * average
    }

    fun judge(meanLuma: Double, variance: Double): CaptureProblem? = when {
        meanLuma < DARK_BELOW -> CaptureProblem.Dark
        meanLuma > BRIGHT_ABOVE -> CaptureProblem.Bright
        variance < BLUR_BELOW -> CaptureProblem.Blurry
        else -> null
    }
}
