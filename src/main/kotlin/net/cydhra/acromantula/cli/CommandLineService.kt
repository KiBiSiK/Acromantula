package net.cydhra.acromantula.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.ShowHelpException
import kotlinx.coroutines.runBlocking
import net.cydhra.acromantula.cli.parsers.*
import net.cydhra.acromantula.commands.CommandDispatcherService
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

/**
 * This service reads from standard in and parses the input as commands that are then dispatched at the
 * [CommandDispatcherService].
 */
object CommandLineService {
    private val logger = LogManager.getLogger()

    @Volatile
    private var running = true

    /**
     * A map of command names to argument parsers, so commands can be parsed from strings
     */
    private val registeredCommandParsers = mutableMapOf<String, (ArgParser) -> WorkspaceCommandParser<*>>()

    /**
     * Command names (and aliases), each entry the name and aliases for one registered parser
     */
    private val commandNames = mutableListOf<String>()

    val commands: List<String> = commandNames

    /**
     * Asynchronous worker for command line
     */
    private val executor = Executors.newSingleThreadExecutor()

    fun initialize() {
        registerCommandParser(::CreateFileCommandParser, "create", "touch")
        registerCommandParser(::DeleteCommandParser, "delete", "del", "rm")
        registerCommandParser(::ListCommandsCommandParser, "commands", "list")
        registerCommandParser(::ImportCommandParser, "import")
        registerCommandParser(::ExportCommandParser, "export")
        registerCommandParser(::ListFilesCommandParser, "ls")
        registerCommandParser(::ListRefsCommandParser, "xref", "references")
        registerCommandParser(::DirectQueryCommandParser, "query")
        registerCommandParser(::ViewCommandCommandParser, "view")
        registerCommandParser(::ExportViewCommandCommandParser, "exportview")
        registerCommandParser(::QuitCommandParser, "quit", "exit")
        registerCommandParser(::ReconstructCommandParser, "reconstruct", "convert")
        registerCommandParser(::ListExportersCommandParser, "exporters", "listexporters")
        registerCommandParser(::RenameCommandParser, "rename", "remap")
        registerCommandParser(::TransformCommandParser, "transform")
        registerCommandParser(::ListViewTypesCommandParser, "views", "viewgenerators", "listviewgenerators")
    }

    fun onStartUp() {
        this.executor.submit {
            commandLineParser()
        }
    }

    fun onShutdown() {
        this.running = false

        this.executor.shutdown()
        logger.info("awaiting CLI service termination (timeout 60 seconds)...")
        if (!this.executor.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.warn("termination of CLI service failed. Forcing...")
            this.executor.shutdownNow()
        }

        logger.info("CLI service terminated")
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

        commandNames += aliases[0] + Arrays.stream(aliases)
            .skip(1)
            .toList()
            .joinToString(prefix = " [", postfix = "]")
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

        try {
            val parser = workspaceParser.build()
            val result = CommandDispatcherService.dispatchCommand(command, parser).await()

            result.onFailure {
                logger.error("error during command evaluation", it)
            }

            @Suppress("UNCHECKED_CAST")
            (workspaceParser as WorkspaceCommandParser<Any?>).report(result)
        } catch (showHelpException: ShowHelpException) {
            val wr = StringWriter()
            showHelpException.printUserMessage(wr, arguments[0], 120)
            logger.info("Command Usage:\n" + wr.buffer.toString())
        }
    }
}