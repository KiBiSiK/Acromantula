package net.cydhra.acromantula.commands

import kotlinx.coroutines.Job
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 *
 */
object CommandDispatcher {

    /**
     * Dispatch a command that originates from anywhere at the workspace. Dispatching it will schedule the command to
     * the worker pool and generate a status code, that can be used to request status information about the command.
     */
    fun dispatchCommand(command: WorkspaceCommand): Job {
        return WorkspaceService.getWorkerPool().launchTask { command.evaluate() }
    }
}