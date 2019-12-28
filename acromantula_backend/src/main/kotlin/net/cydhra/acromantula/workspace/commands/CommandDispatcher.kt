package net.cydhra.acromantula.workspace.commands

import net.cydhra.acromantula.workspace.WorkspaceClient

/**
 * As part of the workspace, a command dispatcher is invoked whenever a service or resource is requested at the
 * workspace. Every request can be formulated as a command. A command that is dispatched at the `CommandDispatcher`
 * will be handled by the worker pool and update its status accordingly. A finished command will fire an event that
 * the requesting service can listen to. A command may specify how and where to fire this event.
 *
 * @param workspaceClient the client at the current workspace
 */
class CommandDispatcher(private val workspaceClient: WorkspaceClient) {

    /**
     * Dispatch a command that originates from anywhere at the workspace. Dispatching it will schedule the command to
     * the worker pool and generate a status code, that can be used to request status information about the command.
     */
    fun dispatchCommand(command: WorkspaceCommand) {
        // TODO
    }
}