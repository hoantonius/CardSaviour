package id.cardframe.app.domain.model

enum class FieldKey {
    Name,
    Title,
    Company,
    Phone,
    Email,
    Website,
    Address,
}

enum class CaptureProblem {
    Dark,
    Bright,
    Blurry,
    NoText,
    Unreadable,
}

data class ContactDraft(
    val fullName: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val phones: List<String> = emptyList(),
    val email: String = "",
    val website: String = "",
    val address: String = "",
    val confidence: Map<FieldKey, Float> = emptyMap(),
    val rawText: String = "",
) {
    fun confidenceOf(key: FieldKey): Float = confidence[key] ?: 0f

    fun withConfidence(key: FieldKey, value: Float): ContactDraft =
        copy(confidence = confidence + (key to value))

    fun hasSignal(): Boolean =
        fullName.isNotBlank() ||
            jobTitle.isNotBlank() ||
            company.isNotBlank() ||
            email.isNotBlank() ||
            website.isNotBlank() ||
            address.isNotBlank() ||
            phones.any { it.isNotBlank() }
}

const val LOW_CONFIDENCE = 0.72f

fun ContactDraft.needsCheck(key: FieldKey, blank: Boolean): Boolean =
    blank || confidenceOf(key) < LOW_CONFIDENCE
