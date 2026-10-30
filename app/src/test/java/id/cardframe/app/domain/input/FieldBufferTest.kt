package id.cardframe.app.domain.input

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldBufferTest {
    @Test
    fun fiftyKeystrokesStayIntactAndDoNotRunHeavyWork() {
        val buffer = FieldBuffer()
        val typed = "Budi Santoso, Head of Enterprise Business, Jakarta 12"
        check(typed.length >= 50) { typed.length }
        replayTyping(buffer, typed)
        assertEquals(typed, buffer.text)
        assertEquals(0, buffer.heavyWorkCount)
        buffer.apply()
        assertEquals(1, buffer.heavyWorkCount)
        assertEquals(typed, buffer.text)
    }
}
