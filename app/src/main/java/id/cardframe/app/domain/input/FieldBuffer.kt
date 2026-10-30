package id.cardframe.app.domain.input

/**
 * What a text field is allowed to do while the user is typing.
 * Heavy work (vCard, QR, file writes) happens only in [apply].
 */
class FieldBuffer {
    var text: String = ""
        private set
    var heavyWorkCount: Int = 0
        private set

    fun onValueChange(value: String) {
        text = value
    }

    fun apply() {
        heavyWorkCount++
    }
}

fun replayTyping(buffer: FieldBuffer, typed: String) {
    var acc = ""
    for (ch in typed) {
        acc += ch
        buffer.onValueChange(acc)
    }
}
