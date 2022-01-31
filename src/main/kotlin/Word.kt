class Word(val text: String) {
    val chars = LetterSet(text)

    override fun toString() = text
}