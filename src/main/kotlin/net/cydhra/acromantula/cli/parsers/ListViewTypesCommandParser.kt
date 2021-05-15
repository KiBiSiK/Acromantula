package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.interpreters.ListViewTypesCommandInterpreter
import org.apache.logging.log4j.LogManager

class ListViewTypesCommandParser(parser: ArgParser) : WorkspaceCommandParser<List<Pair<String, String>>> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override fun build() = ListViewTypesCommandInterpreter()

    override fun report(result: Result<List<Pair<String, String>>>) {
        result.onSuccess { response ->
            logger.info(
                "available view generators:\n${
                    response.joinToString("\n") { (name, type) -> "> \"$name\" (generates $type)" }
                }"
            )
        }
    }
}