/**
 * Trial class - represents one attempt at guessing a word. Contains the word,
 * and the score for each letter represented as:
 *
 *  1 - this position matches
 *  0 - this letter matches nowhere
 * -1 - this letter matches but not at this position
 *
 * It can be constructed from just a word, and completed by calling compare(), or
 * from a string representing a match in the form:
 *
 * aB+Cde where:
 *
 * - a lower case letter means a complete miss - letter not present
 * - + means an exact match for the following letter
 * - an upper case letter means a match but not here
 *
 * Iterating over a Trial object returns a 3-tuple (actually a
 * Trial.IteratorValue) with the position, the letter and its score.
 */

class Trial (givenWord: String?=null, text: String?=null) : Iterable<Trial.IteratorValue> {
    data class IteratorValue(val index: Int, val char: Char, val score: Int)
    class TrialIterator(val trial: Trial) : Iterator<IteratorValue> {
        private var index = 0
        override fun hasNext() = index < trial.size
        override fun next() =
            IteratorValue(index, trial.word[index], trial.scores[index])
                .also { ++index }
    }

    constructor(word: String, givenScores: Iterable<Int>) : this(givenWord=word) {
        scores = givenScores.toList()
    }

    val word: String = givenWord ?: parse(text ?: "")
    lateinit var scores: List<Int>; private set
    val size = word.length

    override fun iterator() = TrialIterator(this)

    /*
     * compare - fill in the scores comparing the Trial's word with the given word
     */

    fun compare(target: String) =
        also {
            val remaining = mutableSetOf<Char>()
            val s = word.toList().zip(target.toList()).map {
                if (it.first == it.second)
                    1
                else {
                    remaining.add(it.second)
                    0
                }
            }
            scores = s.zip(word.toList()).map {
                if (it.first == 0 && remaining.contains(it.second)) -1 else it.first
            }
        }

    /*
     * parse - parse a sting representation of the word and its score, in the
     * format described in the header comment
     */

    fun parse(text: String): String =
        let {
            var hit = false
            text.mapNotNull { ch ->
                when {
                    ch == '+' -> let { hit = true; null }
                    !ch.isLetter() -> null
                    hit -> let { hit = false; ch.lowercase() to +1 }
                    ch.isLowerCase() -> ch to 0
                    ch.isUpperCase() -> ch.lowercase() to -1
                    else -> null
                }
            }.also {
                scores = it.map { pair -> pair.second }
            }.map {
                it.first
            }
        }.joinToString("")

    /*
     * match - return true iff this Trial matches the given word, taking into
     * account the scores. It's made complicated by the fact that a letter is
     * a non-match even if the same letter is present elsewhere and matches.
     */

    fun match(target: String) =
        let {
            val letters = LetterSet(zip(target.toList(), scores).filter{ it.second != 1}.map{it.first})
            zip(word.toList(), target.toList(), scores)
                .fold(true){ prev, v ->
                    prev && let {
                        val (w, t, s) = v   // w=letter from our own word, t=letter from target, s=score
                        when {
                            s > 0 && w != t        -> false
                            s == 0 && w in letters -> false
                            s < 0 && w !in letters -> false
                            s <= 0 && w == t       -> false
                            else                   -> true
                        }
                    }
                }
        }

    /*
     * toString - generate the string representation of the word and its score
     */

    override fun toString() =
        word.toList().zip(scores).joinToString("") {
            when {
                it.second > 0 -> "+${it.first.uppercase()}"
                it.second == 0 -> it.first.lowercase()
                it.second < 0 -> it.first.uppercase()
                else -> ""
            }
        }

    /*
     * toStyledText - return a StyledText object representing the word with the
     * letters colored according to their score
     */

    fun toStyledText() =
        StyledText(word.toList().zip(scores)
            .map {
                when {
                    it.second > 0  -> StyledText(it.first.uppercase(), color="green")
                    it.second == 0 -> StyledText(it.first.lowercase(), color="grey")
                    it.second < 0  -> StyledText(it.first.uppercase(), color="orange")
                    else           -> StyledText()  // can't happen
                }
            })

    /*
     * find - find the selection of words from the vocabulary that match this Trial
     */

    fun find(vocab: Vocabulary) =
        vocab.findBest(this).filter { match(it.text) }.toSet()

    /*
     * isMatched - return true iff this trial has a perfect match on every letter
     */

    fun isMatched() = scores.sum()==scores.size

    /*
     * hash - return a hash value for the scores for this word
     */

    fun hash() =
        scores.fold(0){ prev, s -> (prev shl 2) + (s+1) }
}

