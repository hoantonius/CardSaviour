package id.cardframe.app.domain.release

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ChangelogTest {
    @Test
    fun changelogMatchesTheAppVersionAndUsesOnlyKnownKinds() {
        val file = ChangelogParser.parse(File("src/main/assets/changelog.json").readText())
        val latest = ChangelogParser.latest(file)
        assertEquals(AppRelease.VERSION, latest.version)
        assertTrue(file.releases.size >= 2)
        file.releases.forEach { release ->
            ChangelogParser.semverRank(release.version)
            assertTrue(release.items.isNotEmpty())
            release.items.forEach { item ->
                assertTrue(item.kind, item.kind in ChangelogParser.kinds)
                assertTrue(item.text.isNotBlank())
            }
        }
        assertTrue(ChangelogParser.semverRank("1.1.0") > ChangelogParser.semverRank("1.0.0"))
    }
}
