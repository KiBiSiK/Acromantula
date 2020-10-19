package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.data.DatabaseClient
import net.cydhra.acromantula.workspace.worker.WorkerPool
import java.net.URL

/**
 * A client that connects to the current workspace. It abstracts the physical location of the workspace, allowing
 * opaque handling of local and remote connections. It offers all resources associated with the workspace connection,
 * including, but not limited to, database access and worker-thread-pool.
 */
abstract class WorkspaceClient(databaseUrl: URL) {

    /**
     * A connection to the workspace database
     */
    val databaseClient = DatabaseClient(databaseUrl)

    /**
     * The thread pools used for work related to workspace content
     */
    val workerPool = WorkerPool()
}

