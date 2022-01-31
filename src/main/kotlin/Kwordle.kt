class Kwordle(var vocab: Vocabulary) {
    lateinit var commandList: CommandList
    lateinit var word: Word
    lateinit var trials: TrialSet

    class Command(command: String,
        help: String,
                  val fn: (List<String>)->Unit) : CommandList.Command(command, help)

    val commands = listOf(
        Command("best", "find best word to try", ::doBest),
        Command("entropy", "show entropy for a word in the current context", ::doEntropy),
        Command("exit", "exit kwordle", ::doNew),
        Command("help", "list commands", ::doHelp),
        Command("new", "start with a new random word", ::doNew),
        Command("remaining", "show remaining words", ::doRemaining),
        Command("reveal", "show the current word (cheat!)", ::doReveal),
        Command("set", "set a known word", ::doSet),
        Command("try", "try a word against the current word", ::doTry),
        Command("undo", "undo last tried word", ::doUndo)
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
        return true
    }

    private fun noArgs(args: List<String>) {
        if (args.size>0) CommandList.error("No arguments required for this command")
    }
    private fun oneArg(args: List<String>): String {
        if (args.size==0) {
            CommandList.error("Argument required for this command")
        } else if (args.size>1) {
            CommandList.error("Too many arguments)")
        }
        return args[0]
    }

    private fun newWord(text: String? = null) {
        word = text?.let{ Word(text) } ?: vocab.choose()
        trials = TrialSet(vocab)
    }


    fun doTry(args: List<String>) {
        val t = Trial(vocab.valid(oneArg(args), mustBeWord = true)).compare(word.text)
        trials.append(t)
        if (t.matched()) {
            if (trials.size==1) {
                print(StyledText("You got lucky! The word is '$word'", color="deep_blue").render())
            } else {
                print(
                    StyledText(
                        "Success! The word is '${word}', found in ${trials.size} attempts",
                        color = "deep_blue"
                    ).render()
                )
            }
            newWord()
        } else {
            print(t.toStyledText().render())
        }
        println(StyledText().render())
    }

    fun doNew(args: List<String>) {
        noArgs(args)
        newWord()
    }

    fun doBest(args: List<String>) {
        val best = trials.findBest()
        println("Best word to use is ${best.first} (entropy = ${best.second})")
    }

    fun doRemaining(args: List<String>) {
        noArgs(args)
        if (trials.size>0) {
            val wordList = trials.matches.take(20).map { it.text }.joinToString(", ")
            val ellipsis = if (trials.matches.size > 20) ", ..." else ""
            println("${trials.matches.size} ${"word".makePlural(trials.matches.size)} still possible: $wordList$ellipsis")
        } else {
            println("All ${vocab.size} words still possible")
        }
    }

    fun doReveal(args: List<String>) {
        noArgs(args)
        println(word)
    }

    fun doEntropy(args: List<String>) {
        val w = vocab.valid(oneArg(args))
        val p = Partition(vocab, w, trials.matches)
        println("Entropy for '$w' is ${p.entropy}")
    }

    fun doSet(args: List<String>) {
        newWord(oneArg(args))
    }

    fun doUndo(args: List<String>) {
        noArgs(args)
        if (trials.size==0) {
            CommandList.error("Nothing to undo")
        }
        trials.truncate()
    }

    fun doHelp(args: List<String>) {
        noArgs(args)
        println(commandList.help().joinToString("\n"))
    }

}

