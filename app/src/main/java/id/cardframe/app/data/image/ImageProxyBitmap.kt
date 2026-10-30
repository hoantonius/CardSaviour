package id.cardframe.app.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

object ImageProxyBitmap {
    fun decode(image: ImageProxy): Bitmap {
        val bitmap = if (image.format == ImageFormat.JPEG || image.planes.size == 1) {
            val buffer = image.planes[0].buffer
            buffer.rewind()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: error("Could not decode the captured photo")
        } else {
            error("Unsupported camera image format ${image.format}")
        }
        val rotation = image.imageInfo.rotationDegrees
        if (rotation == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
