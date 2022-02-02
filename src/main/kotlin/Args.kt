
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlin.properties.Delegates
import kotlin.system.exitProcess

class Args() {
    lateinit var command: String; private set
    lateinit var server: String; private set

    val verbose by parser.flagging("-v", "--verbose", help = "say more about what's happening")
    val dictionary by parser.storing("-d", "--dictionary", help = "dictionary filename")
        .default("")
    val lengthStr: String by parser.storing("-l", "--length", help = "length of words to use")
        .default("5")
    val yesColor by parser.flagging("-c", "--color", help = "force color in output")
    val noColor by parser.flagging("-C", "--nocolor", help = "disable color in output")
    var length = 0; private set
    var color = false; private set

    private fun parse(): Args =
        also {
            parser.force()
            try {
                length = lengthStr.toInt()
            } catch(e: Exception) {
                println("length must be a number")
                exitProcess(1)
            }
            color = !noColor
        }

    companion object {
        lateinit var parser: ArgParser
        lateinit var theArgs: Args
        fun parse(cmdargs: Array<String>): Args {
            parser = ArgParser(cmdargs)
            theArgs = Args()
            return theArgs.parse()
        }
    }
}

