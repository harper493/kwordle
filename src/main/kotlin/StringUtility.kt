import java.text.SimpleDateFormat
import java.util.Date

/**
 * Split a string into two at the given [index]
 */

fun String.splitAt(index: Int) =
    if (index < length) Pair(take(index), drop(index)) else Pair(this, "")

/**
 * Split a string into multiple pieces at the given locations. Any index
 * which is out of range or out of order is ignored.
 */

fun String.splitAt(indices: Iterable<Int>): List<String> {
    var prevSplit = 0
    return indices
        .filter{it in 1..(this.length)}
        .makeAscending()
        .map{ index ->
            this.drop(prevSplit)
                .take(index - prevSplit)
                .also{ prevSplit = index }
        }
        .append(this.drop(prevSplit))
}

/**
 * Given a [splitter] function that will divide a string in two, apply it repeatedly
 * to break the string into multiple pieces wherever the splitter applies.
 */

fun String.splitBy(splitter: (String)->Pair<String,String>): List<String> =
    if (isNotEmpty()) {
        splitter(this).let {
            listOf(it.first).append(if (it.first.isNotEmpty()) it.second.splitBy(splitter) else listOf())
        }
    } else listOf()

/**
 * Given a splitter function and a target size, split the string into pieces no
 * larger than the given size, according to the splitter function, if possible.
 */

fun String.splitUsing(splitter: (String)->Pair<String,String>, size: Int): List<String> {
    val substrs = splitBy { splitter(it) }
    return splitAt(substrs.map{it.length}.runningReduceLimit(size).runningReduce{ a,b -> a+b})
}

/**
 * Returns a comma (or other [delimiter]) separated list with the addition of one or more
 * new items. The result is null if it would otherwise be empty.
 */

fun addToTextList(old: String?, new: String, delimiter: String = ",") =
    (old ?: "")
        .split(delimiter)
        .filter{ it.isNotEmpty() }
        .toMutableList()
        .also { it.addAll(new.split(delimiter)) }
        .joinToString(delimiter)
        .ifBlank { null }

/**
 * Return time/date in format yyyy-mm-dd hh:mm:ss
 */

fun getDateTime() =
    SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date())

/*
* Convert just the first character of a string to uppercase
 */

fun String.uppercaseFirst() = take(1).toUpperCase() + drop(1)

/**
 * Get a line of input from the user, with prompt
 */

fun getUserInput(prompt: String): String {
    print(prompt)
    return readLine() ?: ""
}

/**
 * Return true iff any of the given characters are present in the string
 */

fun String.containsAnyOf(chars: String) =
    chars.fold(false) { b, c -> b || c in this }

/**
 * Pad a sting to fit the given [width]. If [width] is negative, left jusify,
 * otherwise right justify. If [width] is zero, do nothing.
 */

fun String.justify(width: Int) = when {
    width < 0 -> padStart(-width)
    width > 0 -> padEnd(width)
    else      -> this // ==0
}

/**
 * Remove from a list of strings any which are a prefix of another
 */

fun Iterable<String>.removePrefixes() =
    removeDuplicates{ a,b -> b.startsWith(a) }

/**
 * Write a string to a file
 */

fun String.writeToFile(filename: String) =
    let { text ->
        with(java.io.PrintWriter(filename)) {
            append(text)
            flush()
            close()
        }
    }

/*

 */

fun String.orBlankIf(pred: ()->Boolean) = if(pred()) "" else this

/**
 * Read the content of a file
 */

fun readFile(filename: String) =
    java.io.File(filename).readText()

fun readFileOrEmpty(filename: String) =
    try {
        readFile(filename)
    } catch(exc: Exception) {
        ""
    }

/*
Get a unique id for something
 */

fun<T> T.adr(): String = Integer.toHexString(System.identityHashCode(this))

/**
 * Replace regex according to given pattern
 */

fun String.regexReplace(rxstr: String,
                        transform: (MatchResult)->String,
                        option: RegexOption?=null): String =
    let {
        (if (option==null) Regex(rxstr) else Regex(rxstr, option))
            .replace(it, transform)
    }

