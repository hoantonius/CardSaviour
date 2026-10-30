package id.cardframe.app.domain.vcard

import id.cardframe.app.domain.model.ContactDraft

object VCardWriter {
    fun build(draft: ContactDraft): String {
        val lines = mutableListOf("BEGIN:VCARD", "VERSION:3.0")
        val name = draft.fullName.trim()
        if (name.isNotEmpty()) {
            lines += "FN:${escape(name)}"
            // Keep the whole name in the given-name slot so Indonesian names are not split.
            lines += "N:;${escape(name)};;;"
        }
        if (draft.company.isNotBlank()) lines += "ORG:${escape(draft.company.trim())}"
        if (draft.jobTitle.isNotBlank()) lines += "TITLE:${escape(draft.jobTitle.trim())}"
        draft.phones.map { it.trim() }.filter { it.isNotEmpty() }.forEach { phone ->
            lines += "TEL;TYPE=VOICE:${escape(phone)}"
        }
        splitPieces(draft.email).filter { it.contains('@') }.forEach { email ->
            lines += "EMAIL;TYPE=INTERNET:${escape(email)}"
        }
        splitPieces(draft.website).forEach { site ->
            lines += "URL:${escape(withScheme(site))}"
        }
        if (draft.address.isNotBlank()) {
            lines += "ADR;TYPE=WORK:;;${escape(draft.address.trim())};;;;"
        }
        lines += "END:VCARD"
        return lines.joinToString("\r\n") { fold(it) } + "\r\n"
    }

    fun fileName(draft: ContactDraft): String {
        val base = draft.fullName.trim().ifBlank { draft.company.trim() }.ifBlank { "contact" }
            .replace(Regex("[^A-Za-z0-9._-]+"), "-")
            .trim('-')
            .take(40)
            .ifBlank { "contact" }
        return "$base.vcf"
    }

    private fun splitPieces(value: String): List<String> =
        value.split(Regex("[,\\n;]+")).map { it.trim() }.filter { it.isNotEmpty() }

    private fun withScheme(site: String): String =
        if (site.startsWith("http://", true) || site.startsWith("https://", true)) site else "https://$site"

    private fun escape(value: String): String =
        value.replace("\\", "\\\\")
            .replace("\r\n", "\\n")
            .replace("\n", "\\n")
            .replace(",", "\\,")
            .replace(";", "\\;")

    private fun fold(line: String): String {
        if (line.length <= 75) return line
        val out = StringBuilder()
        var index = 0
        var limit = 75
        while (index < line.length) {
            val end = minOf(line.length, index + limit)
            if (out.isNotEmpty()) out.append("\r\n ")
            out.append(line, index, end)
            index = end
            limit = 74
        }
        return out.toString()
    }
}
