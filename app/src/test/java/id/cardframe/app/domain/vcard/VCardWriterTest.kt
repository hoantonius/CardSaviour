package id.cardframe.app.domain.vcard

import id.cardframe.app.domain.model.ContactDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VCardWriterTest {
    @Test
    fun writesTheContactWithoutSplittingTheName() {
        val card = VCardWriter.build(
            ContactDraft(
                fullName = "Ho, Antonius",
                jobTitle = "Head",
                company = "PT Contoh",
                phones = listOf("+62 812", "021"),
                email = "a@contoh.co.id",
                website = "contoh.co.id",
                address = "Jl. Sudirman, Jakarta",
            ),
        )
        assertTrue(card.startsWith("BEGIN:VCARD\r\n"))
        assertTrue(card.trimEnd().endsWith("END:VCARD"))
        assertTrue(card.contains("FN:Ho\\, Antonius"))
        assertTrue(card.contains("N:;Ho\\, Antonius;;;"))
        assertTrue(card.contains("TEL;TYPE=VOICE:+62 812"))
        assertTrue(card.contains("TEL;TYPE=VOICE:021"))
        assertTrue(card.contains("URL:https://contoh.co.id"))
        assertTrue(card.contains("ADR;TYPE=WORK:;;Jl. Sudirman\\, Jakarta;;;;"))
        assertFalse(card.contains("TEL;TYPE=VOICE:\r\n"))
    }

    @Test
    fun fileNameFallsBackWhenTheNameIsEmpty() {
        assertEquals("contact.vcf", VCardWriter.fileName(ContactDraft()))
        assertEquals("Siti-Aminah.vcf", VCardWriter.fileName(ContactDraft(fullName = "Siti Aminah")))
    }
}
