class Vocabulary(val length: Int) : Iterable<Word> {

    lateinit var words: Set<Word>; private set
    lateinit var justWords: Set<String>; private set
    var index = mutableMapOf<Pair<Char, Int>, MutableSet<Word>>(); private set
    val size get() = words.size
    override fun iterator() = words.iterator()

    fun load(wordList: Iterable<String>) =
        also {
            words = wordList.filter{it.length==length || length==0}
                .map{Word(it)}
                .also{ it.forEach{ addToIndex(it) } }
                .toSet()
            justWords = words.map{ it.text }.toSet()
        }

    fun loadFile(fileName: String) =
        also {
            load(java.io.File(fileName).readLines())
        }

    fun choose() =
        words.random()

    fun findBest(trial: Trial) =
        trial.fold(words) { prev, iter ->
            when {
                iter.score > 0 -> index[iter.char to iter.index]
                iter.score < 0 -> index[iter.char to -1]
                else -> null
            }.let { if (it?.size ?: words.size + 1 < prev.size) it!! else prev }
        }

    fun valid(text: String, mustBeWord: Boolean=false) =
        let {
            when  {
                text.length < length && length>0 ->
                    CommandList.error("Word is too short")
                text.length > length && length>0 ->
                    CommandList.error("Word is too long")
                mustBeWord && text !in justWords ->
                    CommandList.error("Not a valid word")
            }
            text
        }

    private fun addToIndex(word: Word)
    {
        word.text.forEachIndexed{ i, ch -> addOneIndex(i, ch, word) }
        word.text.toSet().forEach{ addOneIndex(-1, it, word) }
    }
    private fun addOneIndex(i: Int, ch: Char, word: Word) {
        index.merge(ch to i, mutableSetOf(word),
            { old, new -> old.also{ old.addAll(new) }})
    }
}