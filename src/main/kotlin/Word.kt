/**
 * Word class - represents a word in (or from) the dictionary, with the LetterSet of the
 * letters it contains
 */

class Word(val text: String) {
    val chars = LetterSet(text)

    override fun toString() = text
}