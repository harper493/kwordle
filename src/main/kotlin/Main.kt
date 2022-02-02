

fun main(cmdArgs: Array<String>) {
    val args = Args.parse(cmdArgs)
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