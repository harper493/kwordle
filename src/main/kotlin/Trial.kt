class Trial (givenWord: String?=null, text: String?=null) : Iterable<Trial.IteratorValue> {
    data class IteratorValue(val index: Int, val char: Char, val score: Int) {}
    class TrialIterator(val trial: Trial) : Iterator<IteratorValue> {
        private var index = 0
        override fun hasNext() = index < trial.size
        override fun next() =
            IteratorValue(index, trial.word[index], trial.scores[index])
                .also { ++index }
    }

    class Possibles(val trial: Trial) : Iterable<Trial> {
        override fun iterator() = PossibleIterator(trial)
    }
    class PossibleIterator(val trial: Trial) : Iterator<Trial> {
        val cp = CartesianProduct(listOf(-1, 0, 1), trial.word.length).iterator()
        override fun hasNext() = cp.hasNext()
        override fun next() =
            let {
                trial.scores = cp.next()
                trial
            }
    }

    val word: String = givenWord ?: parse(text ?: "")
    lateinit var scores: List<Int>; private set
    val size = word.length

    override fun iterator() = TrialIterator(this)

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


    fun match(target: String) =
        let {
            val letters = target.toSet()
            zip(word.toList(), target.toList(), scores)
                .fold(true){ prev, v ->
                    prev && let {
                        val (w, t, s) = v
                        when {
                            s > 0 && w != t -> false
                            s == 0 && w in letters -> false
                            s < 0 && !(w in letters) -> false
                            s < 0 && w == t -> false
                            else -> true
                        }
                    }
                }
        }.also{ it }

    override fun toString() =
        word.toList().zip(scores)
            .map {
                when {
                    it.second > 0 -> "+${it.first.uppercase()}"
                    it.second == 0 -> it.first.lowercase()
                    it.second < 0 -> it.first.uppercase()
                    else -> ""
                }
            }.joinToString("")

    fun toStyledText() =
        StyledText(word.toList().zip(scores)
            .map {
                when {
                    it.second > 0 -> StyledText(it.first.uppercase(), color="green")
                    it.second == 0 -> StyledText(it.first.lowercase(), color="grey")
                    it.second < 0 -> StyledText(it.first.uppercase(), color="orange")
                    else -> StyledText()
                }
            })

    fun find(vocab: Vocabulary) =
        vocab.findBest(this).filter { match(it.text) }.toSet()

    fun possibles() = Possibles(this)

    fun matched() = scores.sum()==scores.size

    fun hash() =
        scores.fold(0){ prev, s -> (prev shl 2) + (s+1) }
}

