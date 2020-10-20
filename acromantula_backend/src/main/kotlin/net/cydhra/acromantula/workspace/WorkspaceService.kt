package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.data.filesystem.ArchiveEntity
import net.cydhra.acromantula.data.filesystem.DirectoryEntity
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
}