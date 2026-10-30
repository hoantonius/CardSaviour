package id.cardframe.app.domain.parse

import id.cardframe.app.domain.model.ContactDraft
import id.cardframe.app.domain.model.FieldKey
import javax.inject.Inject

class CardParser @Inject constructor() {

    fun parse(raw: String): ContactDraft {
        val source = raw.replace('\u00A0', ' ').trim()
        if (source.isEmpty()) return ContactDraft()

        val emails = EMAIL.findAll(source).map { it.value }.distinct().toList()
        var working = source
        emails.forEach { working = working.replace(it, " ") }

        val phones = mutableListOf<String>()
        PHONE.findAll(working).map { it.value }.distinct().forEach { raw ->
            val cleaned = cleanPhone(raw)
            if (digitCount(cleaned) in 8..15 && cleaned !in phones) {
                phones += cleaned
                working = working.replace(raw, " ")
            }
        }

        val websiteMatches = URL.findAll(working).map { it.value }.distinct().toList()
        val websites = websiteMatches
            .map { it.trim().trimEnd('.', ',', ';') }
            .filter { it.contains('.') && !it.contains('@') }
        websiteMatches.forEach { working = working.replace(it, " ") }

        val titles = mutableListOf<String>()
        val companies = mutableListOf<String>()
        val addresses = mutableListOf<String>()
        val pool = mutableListOf<String>()

        working.lines().forEach { original ->
            val line = LABEL_PREFIX.replace(original.trim(), "").trim().trim(':', '-', '|', '•', '.')
            if (line.length < 2 || line.none { it.isLetterOrDigit() }) return@forEach
            when {
                looksLikeTitle(line) -> titles += line
                looksLikeCompany(line) -> companies += line
                looksLikeAddress(line) -> addresses += line
                else -> pool += line
            }
        }

        var name = ""
        var nameConfidence = 0f
        val personIndex = pool.indexOfFirst { looksLikePerson(it) }
        if (personIndex >= 0) {
            name = pool.removeAt(personIndex)
            nameConfidence = 0.9f
        } else if (pool.isNotEmpty()) {
            name = pool.removeAt(0)
            nameConfidence = 0.45f
        }

        val company: String
        val companyConfidence: Float
        if (companies.isNotEmpty()) {
            company = companies.joinToString(", ")
            companyConfidence = 0.9f
        } else if (pool.isNotEmpty() && pool.first().length <= 48) {
            company = pool.removeAt(0)
            companyConfidence = 0.48f
        } else {
            company = ""
            companyConfidence = 0f
        }

        val title = titles.joinToString(", ")
        val titleConfidence = if (titles.isNotEmpty()) 0.86f else 0f

        val guessedAddress = pool.filter { it.length > 2 }
        val addressParts = addresses + guessedAddress
        val address = addressParts.joinToString(", ")
        val addressConfidence = when {
            addresses.isNotEmpty() -> 0.8f
            guessedAddress.isNotEmpty() -> 0.42f
            else -> 0f
        }

        val phoneConfidence = when {
            phones.isEmpty() -> 0f
            phones.all { digitCount(it) >= 10 } -> 0.9f
            else -> 0.62f
        }

        return ContactDraft(
            fullName = name,
            jobTitle = title,
            company = company,
            phones = phones,
            email = emails.joinToString("\n"),
            website = websites.joinToString("\n"),
            address = address,
            confidence = mapOf(
                FieldKey.Name to nameConfidence,
                FieldKey.Title to titleConfidence,
                FieldKey.Company to companyConfidence,
                FieldKey.Phone to phoneConfidence,
                FieldKey.Email to if (emails.isNotEmpty()) 0.95f else 0f,
                FieldKey.Website to if (websites.isNotEmpty()) 0.9f else 0f,
                FieldKey.Address to addressConfidence,
            ),
            rawText = source.take(2_000),
        )
    }

    private fun looksLikePerson(line: String): Boolean {
        if (line.length !in 5..48) return false
        if (line.any { it.isDigit() }) return false
        val parts = line.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (parts.size !in 2..5) return false
        return parts.all { token ->
            val word = token.trim(',', '.', '\'')
            word.lowercase() in PARTICLES || word.firstOrNull()?.isUpperCase() == true
        }
    }

    private fun looksLikeTitle(line: String): Boolean {
        if (line.length > 60) return false
        val words = line.lowercase().split(Regex("[^a-z]+")).filter { it.length > 1 }
        return words.any { it in TITLE_WORDS }
    }

    private fun looksLikeCompany(line: String): Boolean = COMPANY.containsMatchIn(line)

    private fun looksLikeAddress(line: String): Boolean = ADDRESS.containsMatchIn(line)

    private fun cleanPhone(raw: String): String =
        raw.trim().trimEnd('.', ',', ';').replace(Regex("[\\s.-]{2,}"), " ")

    private fun digitCount(value: String): Int = value.count { it.isDigit() }

    private companion object {
        val EMAIL = Regex("""[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}""", RegexOption.IGNORE_CASE)
        val PHONE = Regex(
            """(?<!\d)(?:\+\d{1,3}[ \t.\-]?)?(?:\(?\d{1,4}\)?[ \t.\-]?)?\d{2,4}[ \t.\-]?\d{2,4}[ \t.\-]?\d{2,4}(?:[ \t.\-]?\d{1,6})?(?!\d)""",
        )
        val URL = Regex(
            """(?i)(?<!@)\b(?:https?://|www\.)?[a-z0-9](?:[a-z0-9-]{0,62}\.)+[a-z]{2,}(?:/[^\s]*)?""",
        )
        val LABEL_PREFIX = Regex(
            """(?i)^(tel|telephone|phone|mobile|handphone|hp|wa|whatsapp|fax|email|e-mail|mail|web|website|url|site|address|alamat|nama|name|title|jabatan|company|perusahaan)\s*[:.\-]?\s*""",
        )
        val COMPANY = Regex(
            """(?i)\b(pt|cv|tbk|ltd|limited|inc|incorporated|corp|corporation|llc|gmbh|pte|bhd)\b\.?""",
        )
        val ADDRESS = Regex(
            """(?i)\b(jl|jalan|street|road|rd|ave|avenue|blvd|suite|floor|lantai|gedung|tower|kav|kawasan|jakarta|bandung|surabaya|medan|singapore|indonesia|selatan|utara|barat|timur|pusat)\b""",
        )
        val PARTICLES = setOf("bin", "binti", "van", "von", "de", "da", "di", "del", "der", "den", "al")
        val TITLE_WORDS = setOf(
            "director", "direktur", "manager", "manajer", "engineer", "insinyur", "head", "kepala",
            "chief", "officer", "founder", "consultant", "konsultan", "specialist", "lead",
            "president", "vp", "vice", "sales", "marketing", "analyst", "architect", "designer",
            "coordinator", "supervisor", "owner", "pemilik", "partner", "principal", "advisor",
        )
    }
}
