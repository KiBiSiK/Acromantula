package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.pool.Task
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * A command to retrieve a list of all tasks currently running.
 */
class ListTasksCommandInterpreter() : WorkspaceCommandInterpreter<List<Task<*>>> {
    override fun evaluate(): List<Task<*>> {
        return WorkspaceService.getWorkerPool().listTasks()
    }
}

