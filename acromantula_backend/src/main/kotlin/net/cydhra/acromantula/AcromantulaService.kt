package net.cydhra.acromantula

import kotlinx.coroutines.runBlocking
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.config.ConfigurationService
import net.cydhra.acromantula.events.ApplicationShutdownEvent
import net.cydhra.acromantula.events.ApplicationStartupEvent
import net.cydhra.acromantula.ipc.IPCService
import net.cydhra.acromantula.plugins.PluginService
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.InputStreamReader

private val logger = LogManager.getLogger("CLI")

fun main() {
    runBlocking {
        EventBroker.registerService(EventBroker)
        EventBroker.registerService(ConfigurationService)
        EventBroker.registerService(PluginService)
        EventBroker.registerService(WorkspaceService)
        EventBroker.registerService(IPCService)

        EventBroker.fireEvent(ApplicationStartupEvent())
    }

    val input = BufferedReader(InputStreamReader(System.`in`))
    var command = ""
    while (true) {
        command = input.readLine()
        if (command != "quit") {
            logger.info("dispatching \"$command\"...")
        } else {
            logger.info("attempting shutdown...")

            runBlocking {
                EventBroker.fireEvent(ApplicationShutdownEvent())
            }
            break
        }
    }

    logger.info("CLI exited.")
}