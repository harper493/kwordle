

fun main(args: Array<String>) {
    val vocab = Vocabulary(5)
    vocab.load(defaultVocabulary.split('\n'))
    val kwordle = Kwordle(vocab)
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