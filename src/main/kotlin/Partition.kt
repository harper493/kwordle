import kotlin.math.ln

/**
 * Partition - split up all currently-possible words according to the
 * response they will get from the Trial class, keyed by a hash
 * of the response.
 *
 * Then (the actual purpose) compute the entropy of the resulting distribution,
 * according to the well-known information theory formula:
 *
 * entropy = -sum( P(i) * log(P(i))
 *
 * where P(i) is the probability of sample - in this case, the
 * size of the sample over the total sample size.
 *
 * This is then normalized so that 1 is perfect entropy, i.e. every
 * non-zero outcome is equally likely.
 */


class Partition(val vocab: Vocabulary, val key: String, val words: Set<Word>) {

    val entropy = evaluateEntropy()

    fun evaluateEntropy() =
        let {
            var totalSize: Double
            -(CartesianProduct(listOf(-1, 0, 1), key.length)
                .map {
                    Trial(key, it)
                        .find(vocab)
                        .intersect(words)
                        .size
                }
                .filter { it > 0.0 }
                .also { totalSize = it.sum().toDouble() }
                .map { it * ln(it.toDouble() / totalSize) }
                .sum()
                    / (totalSize * ln(totalSize)))
        }

}

