package id.cardframe.app.domain.parse

import id.cardframe.app.domain.model.FieldKey
import id.cardframe.app.domain.model.LOW_CONFIDENCE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardParserTest {
    private val parser = CardParser()

    @Test
    fun readsAFullIndonesianCard() {
        val draft = parser.parse(
            """
            Antonius Ho
            Head of Enterprise Business Unit
            PT Contoh Jaya Tbk
            +62 812-3456-7890
            021-5551234
            antonius.ho@contoh.co.id
            www.contoh.co.id
            Jl. Jend. Sudirman Kav. 52-53
            Jakarta Pusat 12190
            """.trimIndent(),
        )

        assertEquals("Antonius Ho", draft.fullName)
        assertEquals("Head of Enterprise Business Unit", draft.jobTitle)
        assertEquals("PT Contoh Jaya Tbk", draft.company)
        assertEquals(listOf("+62 812-3456-7890", "021-5551234"), draft.phones)
        assertEquals("antonius.ho@contoh.co.id", draft.email)
        assertTrue(draft.website.contains("contoh.co.id"))
        assertTrue(draft.address.contains("Sudirman"))
        assertTrue(draft.address.contains("Jakarta"))
        assertTrue(draft.confidenceOf(FieldKey.Name) >= LOW_CONFIDENCE)
        assertTrue(draft.confidenceOf(FieldKey.Company) >= LOW_CONFIDENCE)
        assertFalse(draft.phones.any { it.contains("12190") })
    }

    @Test
    fun stripsLabelsAndKeepsThePerson() {
        val draft = parser.parse(
            """
            Name: Siti Aminah
            Title: Finance Manager
            Company: CV Mawar
            Mobile: 081234567890
            Email: siti@mawar.id
            Web: mawar.id
            Address: Jl. Melati No. 8, Bandung
            """.trimIndent(),
        )

        assertEquals("Siti Aminah", draft.fullName)
        assertEquals("Finance Manager", draft.jobTitle)
        assertEquals("CV Mawar", draft.company)
        assertEquals(listOf("081234567890"), draft.phones)
        assertEquals("siti@mawar.id", draft.email)
        assertEquals("mawar.id", draft.website)
        assertTrue(draft.address.contains("Bandung"))
        assertFalse(draft.email.contains("mawar.id\n") && draft.website.isBlank())
    }

    @Test
    fun emptyScanHasNoSignal() {
        val draft = parser.parse("||| ---")
        assertFalse(draft.hasSignal())
        assertTrue(draft.confidenceOf(FieldKey.Name) < LOW_CONFIDENCE)
    }

    @Test
    fun doesNotTreatEmailAsWebsite() {
        val draft = parser.parse("Budi Santoso\nSales Manager\nbudi@acme.com")
        assertEquals("budi@acme.com", draft.email)
        assertTrue(draft.website.isBlank())
        assertEquals("Budi Santoso", draft.fullName)
    }
}
