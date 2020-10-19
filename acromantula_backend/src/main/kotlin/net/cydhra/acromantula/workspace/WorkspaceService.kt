package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.data.filesystem.ArchiveEntity
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
}