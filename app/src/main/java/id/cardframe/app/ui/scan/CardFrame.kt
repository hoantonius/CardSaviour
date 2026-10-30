package id.cardframe.app.ui.scan

import android.graphics.Bitmap
import android.graphics.RectF
import kotlin.math.min

object CardFrame {
    private const val WIDTH_FRACTION = 0.88f
    private const val ASPECT = 1.586f
    private const val MAX_HEIGHT_FRACTION = 0.72f

    fun rect(width: Float, height: Float): RectF {
        var cardW = width * WIDTH_FRACTION
        var cardH = cardW / ASPECT
        if (cardH > height * MAX_HEIGHT_FRACTION) {
            cardH = height * MAX_HEIGHT_FRACTION
            cardW = cardH * ASPECT
        }
        val left = ((width - cardW) / 2f).coerceAtLeast(0f)
        val minTop = height * 0.04f
        val maxTop = (height - cardH - minTop).coerceAtLeast(minTop)
        val top = ((height - cardH) / 2f - height * 0.03f).coerceIn(minTop, maxTop)
        return RectF(left, top, left + cardW, top + cardH)
    }

    fun crop(bitmap: Bitmap): Bitmap {
        val frame = rect(bitmap.width.toFloat(), bitmap.height.toFloat())
        val left = frame.left.toInt().coerceIn(0, bitmap.width - 1)
        val top = frame.top.toInt().coerceIn(0, bitmap.height - 1)
        val width = min(frame.width().toInt(), bitmap.width - left).coerceAtLeast(1)
        val height = min(frame.height().toInt(), bitmap.height - top).coerceAtLeast(1)
        if (left == 0 && top == 0 && width == bitmap.width && height == bitmap.height) return bitmap
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }
}
