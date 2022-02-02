/**
 * TrialSet - representation of a series of trials against an unknown word.
 *
 * Holds each attempted Trial, and also maintains a set of possible words, which
 * starts off as all word in the vocabulary.
 */

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.GlobalScope

class TrialSet(val vocab: Vocabulary, text: String?=null) : Iterable<Trial> {
    val trials = if (text.isNullOrEmpty()) mutableListOf() else parse(text)
    val size get() = trials.size
    var matches = vocab.words; private set

    /*
     * iterator - return each of the Trial objects in sequence
     */

    override fun iterator() = trials.iterator()

    /*
     * parse - convert a string representing one or more trials, separated by
     * commas, into the corresponding sequence of Trial objects
     */

    fun parse(text: String): MutableList<Trial> =
        text.split(",").map{ Trial(text=it) }.toMutableList().also{ match() }

    fun append(trial: Trial) =
        also{
            trials.add(trial)
            match()
        }

    fun truncate() =
        also {
            trials.removeLast(1)
            match()
        }

    /*
     * match - generate the list of matched words, from the inetersection of
     * possible matches for each contained Trial object
     */

    fun match() =
        let {
            val first: Set<Word>? = null
            matches = trials.fold(first)
                { prev, next ->
                    let {
                        val thisFind = next.find(vocab).toSet()
                        prev?.intersect(thisFind) ?: thisFind
                    }
            } ?: setOf()
            matches
        }

    /*
     * findBest - find twh best word for distinguishing the remaining
     * possibilities. Do this by considering every word in the vocabulary
     * that has at least four letters in common with the remaining letters,
     * using the Partition class to find the entropy of the result
     * it generates. Pick the word with the highest entropy.
     *
     * This is very compute intensive, so it is split among several (20)
     * coroutines to take advantage of a multi-core system.
     *
     * The auxiliary function findBestSome does the work of evaluating
     * partitions for the given subset of the vocabulary.
     */

    fun findBest() =
        let {
            val goodLetters = LetterSet(matches)
            val words = vocab.filter { word ->
                (goodLetters and word.chars).size >= 4
            }
            runBlocking {
                vocab
                    .filter { (goodLetters and it.chars).size >= 4 }
                    .chunked(words.size / 20)
                    .map{ GlobalScope.async{ findBestSome(it, it.first().text) } }
                    .fold(Pair("", 0.0))
                    { prev, def ->
                        let {
                            val r = def.await()
                            if (prev.second < r.second) r else prev
                        }
                    }
            }
        }

    fun findBestSome(words: Iterable<Word>, tag: String) =
        words.fold(Pair("", 0.0))
        { prev, word ->
            let {
                val e = Partition(vocab, word.text, matches).entropy
                if (prev.second < e) Pair(word.text, e) else prev
            }
        }.also{ if (false && tag.isNotEmpty()) println("completed $tag") }

    /*
     * findBestSlow - single thread version of findBest
     */

    fun findBestSlow() =
        let {
            val goodLetters = LetterSet(match())
            var initial = "!"
            vocab.filter{ word ->
                let {
                    val intersect = goodLetters and LetterSet(word)
                    intersect.size >= 4
                }
            }.also{ println("considering ${it.size} words... ")}
                .fold(Pair("", 0.0))
                { prev, word ->
                    let {
                        if (!word.text.startsWith(initial)) {
                            initial = word.text.subSequence(0, 1) as String
                            print(initial)
                        }
                        val e = Partition(vocab, word.text, matches).entropy
                        if (prev.second < e) Pair(word.text, e) else prev
                    }
                }.also{ println() }
        }

    /*
    * letterTypes - return four LetterSets as a LetterTypes (see below)
     */

    fun letterTypes() =
        let {
            val placed = LetterSet()
            val unused = LetterSet()
            val used = LetterSet()
            trials.map{ trial -> trial.map{
                when (it.score) {
                    1 -> placed.insert(it.char)
                    0 -> unused.insert(it.char)
                    -1 -> used.insert(it.char)
                    else -> {}
                }
            }
            }
            used.remove(placed)
            LetterTypes(placed, used, unused,
                LetterSet.all.remove(placed).remove(used).remove(unused))
        }

    /*
     * LetterTypes - four LetterSets, one for each of:
     *
     * placed  - letters whose place is known
     * used    - letters used but whose place is not known
     * unused  - letters known not to be used
     * unknown - letters about which nothing is known
     */

    data class LetterTypes(val placed: LetterSet,
                           val used: LetterSet,
                           val unused: LetterSet,
                           val unknown: LetterSet)
}