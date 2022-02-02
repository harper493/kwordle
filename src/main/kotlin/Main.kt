import kotlin.system.exitProcess

fun main(cmdArgs: Array<String>) {
    lateinit var args: Args
    try {
        args = Args.parse(cmdArgs)
    } catch (e: Exception) {
        println(e.message)
        exitProcess(1)
    }
    val vocab = Vocabulary(args.length)
    if (args.dictionary.isEmpty()) {
        vocab.load(defaultVocabulary.split('\n'))
    } else {
        vocab.loadFile(args.dictionary)
    }
    val kwordle = Kwordle(vocab, args)
    StyledText.setRenderer(if (args.color) "ISO6429" else "None")
    while (true) {
        print("kwordle> ")
        try {
            if (!kwordle.exec(readLine() ?: break)) {
                break
            }
        } catch(e: CommandError) {
            print(StyledText(e.message ?: "", color = "red"))
            println(StyledText())
        }
    }
}