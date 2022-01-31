
class LetterSet(chars: Iterable<Char>?=null, value: Int=0) : Iterable<Char> {
    private val bits = chars?.fold(0){ prev, ch ->prev or (1 shl (toBit(ch))) } ?: value

    override fun iterator() =
        toString().iterator()

    override fun toString() =
        toStringOne(bits, 0)

    fun contains(ch: Char) = (bits and toBit(ch)) != 0
    fun match(str: String) = str.fold(true){ prev, ch -> prev and contains(ch) }
    fun empty() = bits==0
    fun singleton() = bits!=0 && ((bits and (bits-1))==0)
    val size: Int = if (empty()) 0 else 1 + LetterSet(value=(bits and (bits-1))).size
    infix fun or(other: LetterSet) = LetterSet(value=(bits or other.bits))
    infix fun and(other: LetterSet) = LetterSet(value=(bits and other.bits))

    constructor(ch: Char) : this(listOf(ch))
    constructor(text: String) : this(text.toList())
    constructor(word: Word) : this(word.text)
    constructor(words: Iterable<Word>) : this(value=
            words.fold(LetterSet(value=0))
            { prev, w ->
                LetterSet(value = (prev.bits or LetterSet(w.text).bits))
            }.bits)

    companion object {
        private fun toBit(ch: Char) = let {
            val b = ch.lowercaseChar().code - 'a'.code
            b
        }
        private fun fromBit(v: Int) = (v + 'a'.code).toChar()
        private fun toStringOne(bits: Int, offset: Int): String =
            (if ((bits and 1)==1) fromBit(offset).toString() else "") +
                    (if (bits==0) "" else toStringOne(bits shr 1, offset+1))
    }
}