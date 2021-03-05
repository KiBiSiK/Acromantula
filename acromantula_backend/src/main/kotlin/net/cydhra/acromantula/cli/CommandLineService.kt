package net.cydhra.acromantula.cli

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.bus.events.ApplicationShutdownEvent
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent
import net.cydhra.acromantula.cli.parsers.*
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.pool.event.TaskFinishedEvent
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
        EventBroker.registerEventListener(TaskFinishedEvent::class, this::onTaskFinished)

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
                    dispatchCommand(command)
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
    fun dispatchCommand(command: String) {
        val arguments = command.split(" ")
        val parserFactory =
            registeredCommandParsers[arguments[0]] ?: error("\"${arguments[0]}\" is not a valid command")
        val workspaceParser = parserFactory.invoke(ArgParser(arguments.subList(1, arguments.size).toTypedArray()))
        val task = CommandDispatcherService.dispatchCommand(workspaceParser.build())
        dispatchedCommandTasks += Pair(task.id, workspaceParser)
    }

    private fun onTaskFinished(event: TaskFinishedEvent) {
        // find the finished task index in the list of tasks scheduled by this service
        val scheduledParser =
            dispatchedCommandTasks.withIndex().find { (_, parserPair) -> parserPair.first == event.taskId }

        // if present, handle the finished command
        if (scheduledParser != null) {
            val (index, tuple) = scheduledParser
            val (taskId, parser) = tuple
            dispatchedCommandTasks.removeAt(index)

            val task = WorkspaceService.getWorkerPool().reap(taskId)!!

            val resultOptional = task.result

            if (resultOptional.isPresent) {
                resultOptional.get().onFailure {
                    logger.error("error during command evaluation", it)
                }

                // this works because of the class contract (that parsers produce workspace
                // commands of the result type they consume. Essentially, I need an existential type here.
                @Suppress("UNCHECKED_CAST")
                (parser as WorkspaceCommandParser<Any?>).report(resultOptional.get())
            } else {
                @Suppress("UNCHECKED_CAST")
                (parser as WorkspaceCommandParser<Any?>).report(Result.success(null))
            }


        }
    }
}