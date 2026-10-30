package id.cardframe.app.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class BitmapDecoder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun decode(uri: Uri): Bitmap {
        val rotation = context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
                else -> 0
            }
        } ?: 0
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val scale = max(info.size.width, info.size.height).toFloat() / MAX_EDGE
            if (scale > 1f) {
                decoder.setTargetSize(
                    (info.size.width / scale).toInt().coerceAtLeast(1),
                    (info.size.height / scale).toInt().coerceAtLeast(1),
                )
            }
        }
        if (rotation == 0) return decoded
        val matrix = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    }

    private companion object {
        const val MAX_EDGE = 2200
    }
}
