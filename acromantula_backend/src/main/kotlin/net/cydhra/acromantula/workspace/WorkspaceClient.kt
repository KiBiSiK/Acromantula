package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.commands.CommandDispatcher
import net.cydhra.acromantula.workspace.worker.WorkerPool
import org.jetbrains.exposed.sql.transactions.TransactionManager

abstract class WorkspaceClient {

    abstract var database: TransactionManager
        protected set

    /**
     * The local command dispatcher that handles incoming requests for the workspace
     */
    val commandDispatcher = CommandDispatcher()

    /**
     * The thread pools used for work related to workspace content
     */
    val workerPool = WorkerPool()
}