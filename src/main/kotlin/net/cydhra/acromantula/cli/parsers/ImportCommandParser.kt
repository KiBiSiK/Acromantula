package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ImportCommandInterpreter
import net.cydhra.acromantula.features.importer.ImporterJob
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager

class ImportCommandParser(parser: ArgParser) : WorkspaceCommandParser<Unit> {
    companion object {
        private val logger = LogManager.getLogger()
    }

    private val directory by parser.storing(
        "-d", "--directory",
        help = "path where to place the file in the workspace file tree. leave empty for workspace root.",
    ).default(null)

    private val fileUrl by parser.positional("URL", help = "URL pointing to the file")

    override fun build(): WorkspaceCommandInterpreter<Unit> {
        val partialResultChannel = Channel<ImporterJob.ImportProgressEvent>(2)
        partialResultChannel.consumeAsFlow().onEach { event ->
            when (event) {
                is ImporterJob.ImportProgressEvent.ImportComplete -> {
                    fun count(file: FileEntity): Int = 1 + file.children.map(::count).sum()

                    logger.info("successfully imported ${count(event.root)} files. Waiting for mapping...")
                }

                else -> {/* do nothing */
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.Default))

        return ImportCommandInterpreter(directory, fileUrl, partialResultChannel)
    }

    override fun report(result: Result<Unit>) {
        logger.info("import of \"$fileUrl\" ${if (result.isSuccess) "successful" else "failed"}")
    }
}