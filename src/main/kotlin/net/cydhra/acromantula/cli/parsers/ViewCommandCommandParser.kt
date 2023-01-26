package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ViewCommandInterpreter
import net.cydhra.acromantula.workspace.disassembly.FileView
import org.apache.logging.log4j.LogManager

class ViewCommandCommandParser(parser: ArgParser) : WorkspaceCommandParser<FileView?> {
    val filePath by parser.positional("FILE", help = "file in workspace to export")

    val type by parser.positional("TYPE", help = "how to generate the view")

    override fun build(): WorkspaceCommandInterpreter<FileView?> = ViewCommandInterpreter(filePath, type)

    override fun report(result: Result<FileView?>) {
        result.onSuccess { result ->
            if (result == null) {
                LogManager.getLogger().info(
                    "cannot create view of type \"$type\" for " +
                            "\"${this@ViewCommandCommandParser.filePath}\""
                )
            } else {
                LogManager.getLogger().info("view available as resource")
            }
        }
    }
}