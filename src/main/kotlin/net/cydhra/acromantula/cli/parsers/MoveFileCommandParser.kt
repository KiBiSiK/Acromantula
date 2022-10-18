package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.MoveFileCommandInterpreter
import org.apache.logging.log4j.LogManager

class MoveFileCommandParser(parser: ArgParser) : WorkspaceCommandParser<Unit> {
    val filePath by parser.positional(
        "FILE", help = "file in workspace to move. If it is a directory, all contents will be moved as well"
    )

    val targetPath by parser.positional(
        "TARGET",
        help = "directory where to move the file. Will throw an error if it is not an existing directory." +
                " Leave empty to move to repository root."
    ).default("")

    override fun build(): WorkspaceCommandInterpreter<Unit> =
        MoveFileCommandInterpreter(null, filePath, null, targetPath)

    override fun report(result: Result<Unit>) {
        result.onSuccess {
            LogManager.getLogger().info("moved successfully")
        }
    }
}