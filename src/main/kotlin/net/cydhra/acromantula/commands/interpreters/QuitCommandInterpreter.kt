package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.events.ApplicationShutdownEvent
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import org.apache.logging.log4j.LogManager

/**
 * End all tasks, shutdown thread pools, close all resources and then quit the application.
 */
class QuitCommandInterpreter : WorkspaceCommandInterpreter<Unit> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override suspend fun evaluate() {
        logger.info("quitting...")
        EventBroker.fireEvent(ApplicationShutdownEvent())
    }
}

