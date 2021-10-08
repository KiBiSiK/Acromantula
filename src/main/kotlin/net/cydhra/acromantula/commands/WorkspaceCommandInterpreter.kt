package net.cydhra.acromantula.commands

/**
 * An interpreter for a user command. The interpreter is generated by some kind of frontend (like a CLI, or a RPC
 * server) and then evaluated by the [CommandDispatcherService].
 */
interface WorkspaceCommandInterpreter<T> {

    /**
     * Evaluate the command. This function is suspendable and should be launched in a cached thread pool. The
     * function returns a result of the command
     */
    suspend fun evaluate(): T
}