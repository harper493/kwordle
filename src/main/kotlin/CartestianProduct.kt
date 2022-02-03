/**
 * CartesianProduct class - return cartesian product of vectors via an iterator.
 *
 * Given a list of lists (or any iterable), return the cartesian product of
 * their contents in the form of an iterator that successively returns
 * the elements of the product.
 *
 * Alternative constructor allows the same list to be repeated.
 *
 * The types of the elements of the lists must all be the same.
 */

class CartesianProduct<T>(val valueList: Iterable<Iterable<T>>) : Iterable<List<T>> {

    class CPIterator<T>(cp: CartesianProduct<T>) : Iterator<List<T>> {
        val first = SubIterator(cp.valueList.iterator())

        override fun hasNext() = first.hasNext()

        override fun next(): List<T> = first.next()!!

        class SubIterator<T>(val input: Iterator<Iterable<T>>) {
            private val myList = input.next()
            private var myIter = myList.iterator()
            val nextIter: SubIterator<T>? =
                if (input.hasNext()) SubIterator(input) else null
            var cached: List<T>? = if (nextIter==null) null else listOf(myIter.next())

            fun hasNext(): Boolean =
                myIter.hasNext() || nextIter?.hasNext() ?: false

            private fun reset(): SubIterator<T> = also {
                myIter = myList.iterator()
                cached = if (nextIter==null) null else listOf(myIter.next())
                nextIter?.reset()
            }

            fun next(): List<T>? =
                let {
                    val nextVal = nextIter?.next()
                    when {
                        nextIter == null && myIter.hasNext() ->
                            listOf(myIter.next())
                        nextIter == null ->
                            null
                        nextVal != null ->
                            cached!! + nextVal
                        myIter.hasNext() ->
                            let {
                                cached = listOf(myIter.next())
                                cached!! + (nextIter.reset().next())!!
                            }
                        else -> null
                    }
                }
        }
    }
    override fun iterator() = CPIterator(this)
    constructor(values: Iterable<T>, repeat: Int) :
            this(List(repeat) { values })

}