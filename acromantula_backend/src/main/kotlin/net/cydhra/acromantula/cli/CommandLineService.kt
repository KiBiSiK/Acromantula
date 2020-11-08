package net.cydhra.acromantula.cli

import kotlinx.coroutines.delay
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.bus.events.ApplicationShutdownEvent
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent
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
                    if (command == "quit") {
                        EventBroker.fireEvent(ApplicationShutdownEvent())
                    } else {
                        CommandDispatcherService.dispatchCommand(command)
                    }
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

}