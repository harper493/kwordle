/**
 * Vocabulary class - represents a dictionary
 *
 * Can be loaded either from a list of words, or from a file
 * containing one word per line.
 *
 * The length parameter is the length of words to be selected,
 * or 0 to select all of them.
 *
 * In addition to the words, it keeps an index of (letter, position)
 * each with a list of the corresponding matching words. There are two
 * extra entries for each letter:
 *
 * (letter, -1) for all words containing it
 * (letter, -2) for all words not containing it
 */

class Vocabulary(val length: Int) : Iterable<Word> {

    lateinit var words: Set<Word>; private set
    lateinit var justWords: Set<String>; private set
    var maxLength = 0; private set
    var index = mutableMapOf<Pair<Char, Int>, Set<Word>>(); private set
    val size get() = words.size
    override fun iterator() = words.iterator()

    fun load(wordList: Iterable<String>) =
        also {
            words = wordList.filter{it.length==length || length==0}
                .map{Word(it)}
                .toSet()
            justWords = words.map{ it.text }.toSet()
            maxLength = if (length>0) length
                        else justWords.maxOf{ it.length }
            LetterSet.all.forEach{ char ->
                index[char to -2] = words.filter{ char !in it.chars }.toSet()
                index[char to -1] = words.filter{ char in it.chars }.toSet()
                (0..maxLength-1).forEach{ idx ->
                    index[char to idx] = words
                        .filter{ it.length >= idx && it.text.get(idx)==char}.toSet()
                }
            }
        }

    fun loadFile(fileName: String) =
        also {
            load(java.io.File(fileName).readLines())
        }

    /*
     * choose - choose a random word from the dictionary
     */

    fun choose() =
        words.random()

    /*
     * findBest - given a Trial, find the best (shortest)
     * list of possible matching words based on the number
     * of words matching each position
     */

    fun findBest(trial: Trial) =
        trial.fold(words) { prev, iter ->
            when {
                iter.score > 0 -> index[iter.char to iter.index]
                iter.score < 0 -> index[iter.char to -1]
                else           -> index[iter.char to -2]
            }.let { if (it?.size ?: words.size + 1 < prev.size) it!! else prev }
        }

    /*
     * valid - validate a string against the dictionary
     */

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
}