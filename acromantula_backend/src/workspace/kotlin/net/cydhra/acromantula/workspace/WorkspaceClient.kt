package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.worker.WorkerPool
import java.net.URL

/**
 * A client that connects to the current workspace. It abstracts the physical location of the workspace, allowing
 * opaque handling of local and remote connections. It offers all resources associated with the workspace connection,
 * including, but not limited to, database access and worker-thread-pool.
 */
internal abstract class WorkspaceClient(databaseUrl: URL) {

    /**
     * A connection to the workspace database
     */
    val databaseClient = DatabaseClient(databaseUrl)

    /**
     * The thread pools used for work related to workspace content
     */
    val workerPool = WorkerPool()

    /**
     * Initialize client connections, resources, etc
     */
    open fun initialize() {
        databaseClient.connect()
    }

    /**
     * Upload a file into the workspace
     *
     * @param fileEntity database entry to reference the file
     * @param content file binary content
     */
    abstract fun uploadFile(fileEntity: FileEntity, content: ByteArray)
}