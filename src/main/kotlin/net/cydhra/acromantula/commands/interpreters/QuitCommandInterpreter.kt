package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.shutdownServer
import org.apache.logging.log4j.LogManager.getLogger as logger

/**
 * End all tasks, shutdown thread pools, close all resources and then quit the application.
 */
class QuitCommandInterpreter : WorkspaceCommandInterpreter<Unit> {

    override val synchronous: Boolean = true

    override suspend fun evaluate() {
        logger().info("quitting...")
        shutdownServer()
    }
}

