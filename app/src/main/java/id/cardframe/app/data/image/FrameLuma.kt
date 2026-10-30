package id.cardframe.app.data.image

import androidx.camera.core.ImageProxy
import id.cardframe.app.domain.image.LumaStats
import kotlin.math.max

object FrameLuma {
    data class Sample(val mean: Double, val variance: Double)

    fun sample(image: ImageProxy): Sample {
        val plane = image.planes.firstOrNull() ?: return Sample(0.0, 0.0)
        val buffer = plane.buffer
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride
        val width = image.width
        val height = image.height
        if (width < 3 || height < 3) return Sample(0.0, 0.0)

        val left = width / 5
        val top = height / 5
        val right = width - left
        val bottom = height - top
        val regionW = (right - left).coerceAtLeast(1)
        val regionH = (bottom - top).coerceAtLeast(1)
        val stepX = max(1, regionW / 80)
        val stepY = max(1, regionH / 48)
        val cols = ((regionW - 1) / stepX) + 1
        val rows = ((regionH - 1) / stepY) + 1
        val luma = IntArray(cols * rows)
        var index = 0
        var y = top
        while (y < bottom && index < luma.size) {
            var x = left
            while (x < right && index < luma.size) {
                val offset = y * rowStride + x * pixelStride
                luma[index++] = if (offset in 0 until buffer.limit()) {
                    buffer.get(offset).toInt() and 0xFF
                } else {
                    0
                }
                x += stepX
            }
            y += stepY
        }
        return Sample(
            mean = LumaStats.mean(luma, index),
            variance = LumaStats.laplacianVariance(luma, cols, rows),
        )
    }
}
