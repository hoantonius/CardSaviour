package id.cardframe.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun QrGlyph(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cell = size.minDimension / 5f
        val blocks = listOf(
            0 to 0, 1 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 1 to 2, 2 to 2,
            4 to 0, 3 to 1, 4 to 2,
            0 to 4, 2 to 3, 4 to 4, 3 to 3,
        )
        blocks.forEach { (x, y) ->
            drawRect(tint, topLeft = androidx.compose.ui.geometry.Offset(x * cell, y * cell), size = androidx.compose.ui.geometry.Size(cell * 0.8f, cell * 0.8f))
        }
    }
}
