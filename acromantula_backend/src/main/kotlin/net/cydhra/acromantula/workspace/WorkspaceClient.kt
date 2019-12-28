package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.worker.WorkerPool
import org.jetbrains.exposed.sql.transactions.TransactionManager

/**
 * A client that connects to the current workspace. It abstracts the physical location of the workspace, allowing
 * opaque handling of local and remote connections. It offers all resources associated with the workspace connection,
 * including, but not limited to, database access and worker-thread-pool.
 */
abstract class WorkspaceClient {

    abstract var database: TransactionManager
        protected set

    /**
     * The thread pools used for work related to workspace content
     */
    val workerPool = WorkerPool()
}