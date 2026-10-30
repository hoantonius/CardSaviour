package id.cardframe.app.domain.image

import id.cardframe.app.domain.model.CaptureProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageGateTest {
    @Test
    fun flatMidGrayIsBlurry() {
        val luma = IntArray(32 * 32) { 128 }
        val mean = LumaStats.mean(luma)
        val variance = LumaStats.laplacianVariance(luma, 32, 32)
        assertEquals(CaptureProblem.Blurry, LumaStats.judge(mean, variance))
    }

    @Test
    fun darkFrameIsReportedBeforeBlur() {
        val luma = IntArray(16 * 16) { 10 }
        assertEquals(CaptureProblem.Dark, LumaStats.judge(LumaStats.mean(luma), 0.0))
    }

    @Test
    fun checkerboardIsSharpEnough() {
        val width = 32
        val height = 32
        val luma = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            if ((x + y) % 2 == 0) 250 else 10
        }
        val mean = LumaStats.mean(luma)
        val variance = LumaStats.laplacianVariance(luma, width, height)
        assertNull(LumaStats.judge(mean, variance))
        assertTrue(variance > LumaStats.BLUR_BELOW)
    }

    @Test
    fun steadyGateFiresOnceThenResets() {
        val gate = SteadyCardGate(framesNeeded = 3, maxLumaDrift = 5.0)
        assertFalse(gate.observe(120.0, acceptable = true))
        assertFalse(gate.observe(121.0, acceptable = true))
        assertTrue(gate.observe(122.0, acceptable = true))
        assertFalse(gate.observe(122.0, acceptable = true))
        gate.reset()
        assertFalse(gate.observe(80.0, acceptable = false))
        assertFalse(gate.observe(80.0, acceptable = true))
    }
}
