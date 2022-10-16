package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.CreateFileCommandInterpreter
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager

class CreateFileCommandParser(parser: ArgParser) : WorkspaceCommandParser<FileEntity> {
    val filePath by parser.storing("-p", "--parent", help = "directory in workspace where to create file").default("")

    val directory by parser.flagging("-d", "--directory", help = "create a directory").default(false)

    val name by parser.positional("NAME", help = "file name")

    override fun build(): WorkspaceCommandInterpreter<FileEntity> =
        CreateFileCommandInterpreter(filePath, name, directory)

    override fun report(result: Result<FileEntity>) {
        result.onSuccess {
            LogManager.getLogger().info("file ${it.name} created")
        }
    }
}