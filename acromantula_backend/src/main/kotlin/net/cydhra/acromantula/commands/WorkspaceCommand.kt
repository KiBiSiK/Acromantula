package net.cydhra.acromantula.commands

import net.cydhra.acromantula.bus.Event

/**
 * A command for the [CommandDispatcherService] that invokes some kind of service within the Workspace. A command is a
 * special kind of [Event].
 */
interface WorkspaceCommand {

    /**
     * Evaluate the command. This function is suspendable and should be launched in a cached thread pool.
     */
    suspend fun evaluate()
}