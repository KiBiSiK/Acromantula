package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.workspace.filesystem.ArchiveEntity
import net.cydhra.acromantula.workspace.filesystem.DirectoryEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.worker.WorkerPool
import java.io.File

/**
 * Facade service for the workspace sub-system. Everything related to data storage and data operation is delegated
 * from here.
 */
object WorkspaceService : Service {

    override val name: String = "workspace-service"

    /**
     * The client of the current workspace connection.
     */
    private lateinit var workspaceClient: WorkspaceClient

    /**
     * Called upon application startup. Load default workspace and subscribe to events if necessary.
     */
    override suspend fun initialize() {
        workspaceClient = LocalWorkspaceClient(File(".tmp"))
        workspaceClient.initialize()
    }

    /**
     * Get the common thread pool for the current workspace
     */
    fun getWorkerPool(): WorkerPool {
        return workspaceClient.workerPool
    }

    /**
     * Add an archive entry into the workspace file tree. Since the archive is only parent to its content, no actual
     * data is associated with it. It is simply a [DirectoryEntity] that has an [ArchiveEntity] associated
     *
     * @param archiveName simple name of the archive
     */
    fun addArchiveEntry(archiveName: String, parent: DirectoryEntity?): DirectoryEntity {
        return workspaceClient.databaseClient.transaction {
            val archive = ArchiveEntity.new {}

            DirectoryEntity.new {
                this.name = archiveName
                this.parent = parent
                this.archive = archive
            }
        }
    }

    /**
     * Add a directory entry into the workspace file tree.
     */
    fun addDirectoryEntry(name: String, parent: DirectoryEntity?): DirectoryEntity {
        return workspaceClient.databaseClient.transaction {
            DirectoryEntity.new {
                this.name = name
                this.parent = parent
            }
        }
    }

    /**
     * Add a file into the workspace. An entry in database is create for reference and the content is uploaded into
     * the workspace.
     *
     * @param name file name
     * @param parent optional parent directory
     * @param content file binary content
     */
    fun addFileEntry(name: String, parent: DirectoryEntity?, content: ByteArray): FileEntity {
        val fileEntity = workspaceClient.databaseClient.transaction {
            FileEntity.new {
                this.name = name
                this.parent = parent
            }
        }

        workspaceClient.uploadFile(fileEntity, content)
        return fileEntity
    }

    fun queryDirectory(path: String): DirectoryEntity {
        TODO()
    }

    fun queryDirectory(id: Int): DirectoryEntity {
        TODO()
    }
}