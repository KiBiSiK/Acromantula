package net.cydhra.acromantula.commands

import com.xenomachina.argparser.ArgParser
import kotlinx.coroutines.Job
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.commands.impl.*
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager

/**
 *
 */
object CommandDispatcherService : Service {

    override val name: String = "command dispatcher"

    private val logger = LogManager.getLogger()

    /**
     * A map of command names to argument parsers, so commands can be parsed from strings
     */
    private val registeredCommandParsers = mutableMapOf<String, (ArgParser) -> WorkspaceCommandArgs>()

    /**
     * Register a command handler and an argument parser for a given name
     */
    fun registerCommandParser(command: String, argumentParser: (ArgParser) -> WorkspaceCommandArgs) {
        logger.trace("registering command parser for $command: [${argumentParser.javaClass.simpleName}]")
        registeredCommandParsers[command] = argumentParser
    }

    /**
     * Dispatch a command that originates from anywhere at the workspace. Dispatching it will schedule the command to
     * the worker pool and generate a status code, that can be used to request status information about the command.
     */
    fun dispatchCommand(command: WorkspaceCommand): Job {
        logger.trace("launching command handler task for $command")
        return WorkspaceService.getWorkerPool().launchTask { command.evaluate() }
    }

    /**
     * Dispatch a command that is parsed from a string. Arguments are split naively at spaces, so complex arguments
     * escaped by double-quotes are not possible using this method. Dispatching it will schedule the command to
     * the worker pool and generate a status code, that can be used to request status information about the command.
     */
    fun dispatchCommand(command: String): Job {
        val arguments = command.split(" ")
        val parser = registeredCommandParsers[arguments[0]] ?: error("\"${arguments[0]}\" is not a valid command")
        return dispatchCommand(parser.invoke(ArgParser(arguments.subList(1, arguments.size).toTypedArray())).build())
    }

    override suspend fun initialize() {
        registerCommandParser("import", ::ImportCommandArgs)
        registerCommandParser("export", ::ExportCommandArgs)
        registerCommandParser("ls", ::ListFilesParser)
        registerCommandParser("query", ::DirectQueryArgs)
        registerCommandParser("view", ::ViewCommandArgs)
        registerCommandParser("exportview", ::ExportViewCommandArgs)
    }
}