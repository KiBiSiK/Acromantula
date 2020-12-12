package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.interpreters.ListViewTypesCommandInterpreter
import org.apache.logging.log4j.LogManager
import java.util.*

class ListViewTypesCommandParser(parser: ArgParser) : WorkspaceCommandParser<List<Pair<String, String>>> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override fun build() = ListViewTypesCommandInterpreter()

    override fun report(result: Optional<out Result<List<Pair<String, String>>>>) {
        val response = result.get()
        if (response.isSuccess) {
            logger.info(
                "available view generators:\n${
                    response.getOrThrow().joinToString("\n") { (name, type) ->
                        "> \"$name\" (generates $type)"
                    }
                }"
            )
        } else {
            logger.error(
                "error while listing view generators", response.exceptionOrNull()!!
            )
        }
    }
}