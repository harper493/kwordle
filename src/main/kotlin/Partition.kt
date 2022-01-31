


class Partition(val vocab: Vocabulary, val key: String, val words: Set<Word>) {

    val result = mutableMapOf<Int, Int>()
    var entropy: Double = 0.0; private set

    init {
        evaluate()
    }

    fun evaluate() {
        Trial(key).possibles().forEach{ trial ->
            result[trial.hash()] = trial.find(vocab).intersect(words).size
        }
        val totalSize = result.values.sum().toDouble()
        val totalSizeDiv = 1.0 / totalSize
        entropy = -totalSizeDiv  *
                result.values
                    .filter{ it > 0.0 }
                    .map{ it * Math.log( it.toDouble() * totalSizeDiv ) }
                    .sum()
        entropy /= Math.log(totalSize)

    }
}

