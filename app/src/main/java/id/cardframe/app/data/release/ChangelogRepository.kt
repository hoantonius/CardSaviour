package id.cardframe.app.data.release

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import id.cardframe.app.domain.release.ChangelogFile
import id.cardframe.app.domain.release.ChangelogParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangelogRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    val file: ChangelogFile = context.assets.open("changelog.json").bufferedReader().use { reader ->
        ChangelogParser.parse(reader.readText())
    }
}
