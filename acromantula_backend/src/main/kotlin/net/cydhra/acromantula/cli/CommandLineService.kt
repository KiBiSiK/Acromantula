package net.cydhra.acromantula.cli

import com.xenomachina.argparser.ArgParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

/**
 * This service reads from standard in and parses the input as commands that are then dispatched at the
 * [CommandDispatcherService].
 */
object CommandLineService : Service {
    override val name: String = "CLI"

    private val logger = LogManager.getLogger()

    @Volatile
    private var running = true

    override suspend fun initialize() {
        EventBroker.registerEventListener(ApplicationStartupEvent::class, this::onStartUp)
        EventBroker.registerEventListener(ApplicationShutdownEvent::class, this::onShutdown)

        registerCommandParser(::ImportCommandCommandParser, "import")
        registerCommandParser(::ExportCommandCommandParser, "export")
        registerCommandParser(::ListFilesCommandParser, "ls")
        registerCommandParser(::DirectQueryCommandParser, "query")
        registerCommandParser(::ViewCommandCommandParser, "view")
        registerCommandParser(::ExportViewCommandCommandParser, "exportview")
        registerCommandParser(::QuitCommandParser, "quit", "exit")
        registerCommandParser(::ReconstructCommandParser, "reconstruct", "convert")
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun onStartUp(@Suppress("UNUSED_PARAMETER") event: ApplicationStartupEvent) {
        WorkspaceService.getWorkerPool().launchTask { parseCommandLine() }
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun onShutdown(@Suppress("UNUSED_PARAMETER") event: ApplicationShutdownEvent) {
        this.running = false
    }

    private suspend fun parseCommandLine() {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        while (this.running) {
            if (reader.ready()) {
                val command = reader.readLine()
                logger.info("command input: \"$command\"...")

                try {
                    dispatchCommand(command)
                } catch (e: IllegalStateException) {
                    logger.error("cannot dispatch command: ${e.message}")
                } catch (e: Exception) {
                    logger.error("command dispatch failed for unexpected reasons", e)
                }
            } else {
                delay(5L)
            }
        }
    }

    /**
     * A map of command names to argument parsers, so commands can be parsed from strings
     */
    private val registeredCommandParsers = mutableMapOf<String, (ArgParser) -> WorkspaceCommandParser>()

    /**
     * Register a command handler and an argument parser for a set of aliases used to invoke them from command line
     */
    fun registerCommandParser(argumentParser: (ArgParser) -> WorkspaceCommandParser, vararg aliases: String) {
        aliases.forEach { command ->
            logger.trace("registering command parser for $command: [${argumentParser.javaClass.simpleName}]")
            registeredCommandParsers[command] = argumentParser
        }
    }


    /**
     * Dispatch a command that is parsed from a string. Arguments are split naively at spaces, so complex arguments
     * escaped by double-quotes are not possible using this method. Dispatching it will schedule the command to
     * the worker pool and generate a status code, that can be used to request status information about the command.
     */
    fun dispatchCommand(command: String): Job {
        val arguments = command.split(" ")
        val parser = registeredCommandParsers[arguments[0]] ?: error("\"${arguments[0]}\" is not a valid command")
        return CommandDispatcherService.dispatchCommand(
            parser.invoke(ArgParser(arguments.subList(1, arguments.size).toTypedArray())).build()
        )
    }
}