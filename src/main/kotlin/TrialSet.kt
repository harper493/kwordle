import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.GlobalScope

class TrialSet(val vocab: Vocabulary, text: String?=null) : Iterable<Trial> {
    val trials = if (text.isNullOrEmpty()) mutableListOf() else parse(text)
    val size get() = trials.size
    var matches = vocab.words; private set

    override fun iterator() = trials.iterator()

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

    fun findBest() =
        let {
            val goodLetters = LetterSet(matches)
            val words = vocab.filter { word ->
                (goodLetters and LetterSet(word)).size >= 4
            }
            val subsets = LetterSet.all.map { it to mutableSetOf<Word>() }.toMap()
            words.forEach { subsets[it.text.first()]!!.add(it) }
            val results = subsets.map{ GlobalScope.async{ findBestSome(it.value, it.key.toString()) } }
            var result = Pair("", 0.0)
            runBlocking {
                result = results.fold(Pair("", 0.0))
                { prev, def ->
                    let {
                        val r = def.await()
                        if (prev.second < r.second) r else prev
                    }
                }
            }
            result
        }

    fun findBestSome(words: Iterable<Word>, tag: String) =
        words.fold(Pair("", 0.0))
        { prev, word ->
            let {
                val e = Partition(vocab, word.text, matches).entropy
                if (prev.second < e) Pair(word.text, e) else prev
            }
        }.also{ if (false && tag.isNotEmpty()) println("completed $tag") }

    fun letterTypes() =
        let {
            var placed = LetterSet()
            var unused = LetterSet()
            var used = LetterSet()
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

    data class LetterTypes(val placed: LetterSet,
                           val used: LetterSet,
                           val unused: LetterSet,
                           val unknown: LetterSet)
}