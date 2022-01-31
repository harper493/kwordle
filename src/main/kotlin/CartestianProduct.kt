


class CartesianProduct<T>(val valueList: Iterable<Iterable<T>>) : Iterable<List<T>> {

    class CPIterator<T>(val cp: CartesianProduct<T>) : Iterator<List<T>> {
        val first = SubIterator<T>(cp.valueList.iterator())

        override fun hasNext() = first.hasNext()

        override fun next(): List<T> = first.next()!!

        class SubIterator<T>(val input: Iterator<Iterable<T>>) {
            val myList = input.next()
            var myIter = myList.iterator()
            val nextIter: SubIterator<T>? =
                if (input.hasNext()) SubIterator<T>(input) else null
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
    override fun iterator() = CPIterator<T>(this)
    constructor(values: Iterable<T>, repeat: Int) :
            this(List(repeat, { values }) ) { }

}