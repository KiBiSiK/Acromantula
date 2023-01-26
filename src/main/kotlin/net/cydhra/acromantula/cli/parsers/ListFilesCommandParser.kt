package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ListFilesCommandInterpreter
import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager

class ListFilesCommandParser(parser: ArgParser) : WorkspaceCommandParser<List<FileEntity>> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    val directoryPath by parser.storing("-d", "-p", "--path", help = "directory path").default(null)

    val directoryId by parser.storing("-i", "--identifier",
        help = "directory identifier",
        transform = { toInt() }).default(null)

    override fun build(): WorkspaceCommandInterpreter<List<FileEntity>> =
        if (directoryPath != null)
            ListFilesCommandInterpreter(directoryPath)
        else
            ListFilesCommandInterpreter(directoryId)

    override fun report(result: Result<List<FileEntity>>) {
        fun dumpView(view: FileViewEntity, prefix: String = ""): String {
            return prefix + "V: " + view.type + "\n"
        }

        fun dumpFileTree(node: FileEntity, prefix: String = ""): String {
            return prefix +
                    node.name + "\n" +
                    node.children.joinToString("") { dumpFileTree(it, prefix + "\t") } +
                    node.views.joinToString("") { dumpView(it, prefix + "\t") }
        }

        result.onSuccess { fileList ->
            val tree = fileList.joinToString("\n", transform = ::dumpFileTree)
            logger.info("File List:\n$tree")
        }
    }
}