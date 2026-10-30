package id.cardframe.app.domain.release

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ChangelogFile(
    val releases: List<ReleaseNote>,
)

@Serializable
data class ReleaseNote(
    val version: String,
    val date: String,
    val items: List<ChangeItem>,
)

@Serializable
data class ChangeItem(
    val kind: String,
    val text: String,
)

object AppRelease {
    const val VERSION = "1.1.0"
    const val VERSION_CODE = 2
}

object ChangelogParser {
    private val json = Json { ignoreUnknownKeys = true }
    val kinds = setOf("New", "Improved", "Fixed")

    fun parse(text: String): ChangelogFile = json.decodeFromString(text)

    fun latest(file: ChangelogFile): ReleaseNote =
        file.releases.maxWith(compareBy { semverRank(it.version) })

    fun semverRank(version: String): Int {
        val parts = version.split('.')
        require(parts.size == 3) { "Expected MAJOR.MINOR.PATCH, got $version" }
        val (major, minor, patch) = parts.map { it.toInt() }
        return major * 1_000_000 + minor * 1_000 + patch
    }
}
