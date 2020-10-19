package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.bus.Service

/**
 * Facade service for the workspace sub-system. Everything related to data storage and data operation is delegated
 * from here.
 */
object WorkspaceService : Service {

    override val name: String = "workspace-service"

    /**
     * The client of the current workspace connection. This is only used internally by the service and hidden from
     * the outside world. Services from outside invoke [net.cydhra.acromantula.workspace.commands.WorkspaceCommand]s
     * at the [commandDispatcher] for any requests.
     */
    private lateinit var workspaceClient: WorkspaceClient

    /**
     * Called upon application startup. Load default workspace and subscribe to events if necessary.
     */
    override suspend fun initialize() {
        workspaceClient = LocalWorkspaceClient()
    }
}