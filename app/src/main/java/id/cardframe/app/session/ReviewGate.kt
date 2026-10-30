package id.cardframe.app.session

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lets the editor refuse a tab change while a field edit is unsaved.
 * [onAttemptLeave] returns true when it kept the user on the editor.
 */
@Singleton
class ReviewGate @Inject constructor() {
    var onAttemptLeave: ((then: () -> Unit) -> Boolean)? = null
}
