package net.cydhra.acromantula.commands

import kotlinx.coroutines.CompletableJob

/**
 * An interpreter for a user command. The interpreter is generated by some kind of frontend (like a CLI, or a RPC
 * server) and then evaluated by the [CommandDispatcherService].
 */
interface WorkspaceCommandInterpreter<T> {

    /**
     * Evaluate the command. This function is suspendable and should be launched in a cached thread pool. The
     * function returns
     *
     * @param supervisor the supervisor that oversees this command's work so the command issuer can await all
     * subsequent tasks
     */
    suspend fun evaluate(supervisor: CompletableJob): T
}