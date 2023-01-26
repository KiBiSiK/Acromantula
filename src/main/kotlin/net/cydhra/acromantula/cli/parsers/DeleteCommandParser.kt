package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.DeleteCommandInterpreter
import org.apache.logging.log4j.LogManager

class DeleteCommandParser(parser: ArgParser) : WorkspaceCommandParser<Unit> {
    val filePath by parser.positional("FILE", help = "file in workspace to delete. If it is a directory, all contents" +
            " will be deleted as well")

    override fun build(): WorkspaceCommandInterpreter<Unit> = DeleteCommandInterpreter(filePath)

    override fun report(result: Result<Unit>) {
        result.onSuccess {
            LogManager.getLogger().info("file deleted")
        }
    }
}