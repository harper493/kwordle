class CommandError(message: String) : Exception(message)

class CommandList(val commands: List<Command>) {

    open class Command(val command: String,
                  val help: String) {

        lateinit var prefix: String; private set

        fun formatHelp() =
            ("%-15s %s").format(command, help)

        fun setPrefix(commands: Iterable<Command>) {
            prefix = commands.filter{ it!=this }.fold(""){
                    prev, cmd -> let {
                val pfx = this.command.commonPrefixWith(cmd.command)
                if (pfx.length > prev.length) pfx else prev
            }
            }
        }
    }

    init{
        commands.forEach{ it.setPrefix(commands) }
    }

    fun find(line: String): Pair<Command?, List<String>> {
        val args = line.split("#")[0].split("\\s+".toRegex())
        val cmd = args[0]
        val chosen = if (cmd.isNotEmpty()) {
            val matches = findCommands(cmd)
            when {
                matches.size==0 -> { error("Invalid command"); null }
                matches.size>1 -> { error("Ambiguous command"); null }
                else -> matches[0]
            }
        } else null
        return chosen to args.drop(1)
    }

    fun help() =
        commands.map{ it.formatHelp() }

    fun getHelp(subject: String) =
        findCommands(subject)
            .map{ it.formatHelp() }
            .let{ if (it.isEmpty()) listOf("No help available for $subject") else it }

    private fun findCommands(cmd: String) =
        commands
            .filter{ cmd.startsWith(it.prefix) and
                    it.command.startsWith(cmd) and
                    (cmd.length <= it.command.length) }

    companion object {
        fun error(msg: String) {
            throw CommandError(msg)
        }
    }

}