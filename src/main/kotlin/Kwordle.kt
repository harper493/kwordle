class Kwordle(var vocab: Vocabulary, val cmdArgs: Args) {
    var commandList: CommandList
    lateinit var word: Word
    lateinit var trials: TrialSet

    class Command(command: String,
        help: String,
                  val fn: (List<String>)->Unit) : CommandList.Command(command, help)

    val commands = listOf(
        Command("best", "find best word to try (slow)", ::doBest),
        Command("entropy", "show entropy for a word in the current context", ::doEntropy),
        Command("exit", "exit kwordle", ::doNew),
        Command("help", "list commands", ::doHelp),
        Command("new", "start with a new random word", ::doNew),
        Command("recap", "show tries so far and letter status", ::doRecap),
        Command("remaining", "show remaining words (cheat!)", ::doRemaining),
        Command("reveal", "show the current word (cheat!)", ::doReveal),
        Command("set", "set a known word", ::doSet),
        Command("try", "try a word against the current word", ::doTry),
        Command("undo", "undo last tried word", ::doUndo),
        Command("words", "add word(s) to dictionary", ::doWords)
    )

    init {
        commandList = CommandList(commands)
        newWord()
    }

    fun exec(line: String): Boolean {
        val (chosen, args) = commandList.find(line)
        if (chosen?.command=="exit") {
            return false
        }
        if (chosen!=null) {
            (chosen as Command).fn(args)
        }
        print(StyledText())
        return true
    }

    private fun noArgs(args: List<String>) {
        if (args.isNotEmpty()) CommandList.error("No arguments required for this command")
    }

    private fun oneArg(args: List<String>): String {
        if (args.isEmpty()) {
            CommandList.error("Argument required for this command")
        } else if (args.size>1) {
            CommandList.error("Too many arguments)")
        }
        return args[0]
    }

    private fun someArgs(args: List<String>) {
        if (args.isEmpty()) {
            CommandList.error("One or more arguments required for this command")
        }
    }

    private fun newWord(text: String? = null) {
        word = text?.let{ Word(text) } ?: vocab.choose()
        trials = TrialSet(vocab)
    }

    fun doTry(args: List<String>) {
        val t = Trial(vocab.valid(oneArg(args), mustBeWord = true)).compare(word.text)
        trials.append(t)
        if (t.isMatched()) {
            if (trials.size==1) {
                output("You got lucky! The word is '$word'")
            } else {
                output("Success! The word is '${word}', found in ${trials.size} attempts")
            }
            newWord()
        } else {
            println(t.toStyledText())
        }
    }

    fun doNew(args: List<String>) {
        noArgs(args)
        newWord()
    }

    fun doRecap(args: List<String>) {
        noArgs(args)
        val types = trials.letterTypes()
        println(trials.joinToString("\n") { it.toStyledText().render() })
        println(StyledText(LetterSet.all.map{
            val letter = it.toString()
            when {
                types.placed.contains(it) -> StyledText(letter.uppercase(), color="green")
                types.used.contains(it) -> StyledText(letter.uppercase(), color="orange")
                types.unused.contains(it) -> StyledText(letter, color="grey")
                else -> StyledText(letter, color="black")
            }
        }))
    }

    fun doBest(args: List<String>) {
        var best: Pair<String, Double>
        if (cmdArgs.verbose) {
            best = trials.findBest(true)
        } else {
            print("Finding best word ")
            ProgressMarker().use {
                best = trials.findBest(false)
            }
        }
        output("\nBest word to use is '${best.first}' (entropy = ${"%.4f".format(best.second)})")
    }

    fun doRemaining(args: List<String>) {
        noArgs(args)
        if (trials.size>0) {
            val wordList = trials.matches.take(20).joinToString(", ") { it.text }
            val ellipsis = if (trials.matches.size > 20) ", ..." else ""
            output("${trials.matches.size} ${"word".makePlural(trials.matches.size)} still possible: $wordList$ellipsis")
        } else {
            output("All ${vocab.size} words still possible")
        }
    }

    fun doReveal(args: List<String>) {
        noArgs(args)
        println(word)
    }

    fun doEntropy(args: List<String>) {
        val w = vocab.valid(oneArg(args))
        val p = Partition(vocab, w, trials.matches)
        output("Entropy for '$w' is ${"%.4f".format(p.entropy)}")
    }

    fun doSet(args: List<String>) {
        newWord(oneArg(args))
    }

    fun doWords(args: List<String>) {
        someArgs(args)
        args.forEach{ vocab.addWord(it) }
    }

    fun doUndo(args: List<String>) {
        noArgs(args)
        if (trials.size==0) {
            CommandList.error("Nothing to undo")
        }
        trials.truncate()
    }

    fun doHelp(args: List<String>) {
        if (args.isEmpty()) {
            println(commandList.help().joinToString("\n"))
        } else {
            println(commandList.getHelp(oneArg(args)).joinToString("\n"))
        }
    }

    fun output(text: String) {
        println(StyledText(text, color="deep_blue"))
    }

}

