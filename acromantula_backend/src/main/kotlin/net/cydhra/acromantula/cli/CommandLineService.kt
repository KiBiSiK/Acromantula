package net.cydhra.acromantula.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.ShowHelpException
import kotlinx.coroutines.runBlocking
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.bus.events.ApplicationShutdownEvent
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent
import net.cydhra.acromantula.cli.parsers.*
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter

/**
 * This service reads from standard in and parses the input as commands that are then dispatched at the
 * [CommandDispatcherService].
 * TODO change this to single thread executor to not starve the worker thread pool
 */
object CommandLineService : Service {
    override val name: String = "CLI"

    private val logger = LogManager.getLogger()

    @Volatile
    private var running = true

    /**
     * A map of command names to argument parsers, so commands can be parsed from strings
     */
    private val registeredCommandParsers = mutableMapOf<String, (ArgParser) -> WorkspaceCommandParser<*>>()

    /**
     * A list of task ids that were scheduled by this service.
     */
    private val dispatchedCommandTasks = mutableListOf<Pair<Int, WorkspaceCommandParser<*>>>()

    override suspend fun initialize() {
        EventBroker.registerEventListener(ApplicationStartupEvent::class, this::onStartUp)
        EventBroker.registerEventListener(ApplicationShutdownEvent::class, this::onShutdown)

        registerCommandParser(::ImportCommandParser, "import")
        registerCommandParser(::ExportCommandParser, "export")
        registerCommandParser(::ListFilesCommandParser, "ls")
        registerCommandParser(::DirectQueryCommandParser, "query")
        registerCommandParser(::ViewCommandCommandParser, "view")
        registerCommandParser(::ExportViewCommandCommandParser, "exportview")
        registerCommandParser(::QuitCommandParser, "quit", "exit")
        registerCommandParser(::ReconstructCommandParser, "reconstruct", "convert")
        registerCommandParser(::ListTasksCommandParser, "tasks", "listtasks")
        registerCommandParser(::ListExportersCommandParser, "exporters", "listexporters")
        registerCommandParser(::ListViewTypesCommandParser, "views", "viewgenerators", "listviewgenerators")
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun onStartUp(@Suppress("UNUSED_PARAMETER") event: ApplicationStartupEvent) {
        WorkspaceService.getWorkerPool().launchTask(initialStatus = "waiting for input", autoReap = true) {
            commandLineParser()
        }
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun onShutdown(@Suppress("UNUSED_PARAMETER") event: ApplicationShutdownEvent) {
        this.running = false
    }

    private fun commandLineParser() {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        while (this.running) {
            if (reader.ready()) {
                val command = reader.readLine()
                logger.info("command input: \"$command\"...")

                try {
                    runBlocking {
                        dispatchCommand(command)
                    }
                } catch (e: IllegalStateException) {
                    logger.error("cannot dispatch command: ${e.message}")
                } catch (e: Exception) {
                    logger.error("command dispatch failed for unexpected reasons", e)
                }
            } else {
                Thread.sleep(5L)
            }
        }
    }

    /**
     * Register a command handler and an argument parser for a set of aliases used to invoke them from command line
     */
    fun <V> registerCommandParser(argumentParser: (ArgParser) -> WorkspaceCommandParser<V>, vararg aliases: String) {
        aliases.forEach { command ->
            logger.trace("registering command parser for $command")
            registeredCommandParsers[command] = argumentParser
        }
    }


    /**
     * Dispatch a command that is parsed from a string. Arguments are split naively at spaces, so complex arguments
     * escaped by double-quotes are not possible using this method. Dispatching it will schedule the command to
     * the worker pool and generate a status code, that can be used to request status information about the command.
     */
    suspend fun dispatchCommand(command: String) {
        val arguments = command.split(" ")
        val parserFactory =
            registeredCommandParsers[arguments[0]] ?: error("\"${arguments[0]}\" is not a valid command")

        val workspaceParser = parserFactory.invoke(ArgParser(arguments.subList(1, arguments.size).toTypedArray()))

        val result = CommandDispatcherService.dispatchCommand(workspaceParser.build()).await()
        val exception = result.exceptionOrNull()
        if (exception is ShowHelpException) {
            val wr = StringWriter()
            exception.printUserMessage(wr, arguments[0], 120)
            logger.info("Command Usage:\n" + wr.buffer.toString())
        } else {
            @Suppress("UNCHECKED_CAST")
            (workspaceParser as WorkspaceCommandParser<Any?>).report(result)
        }
    }
}