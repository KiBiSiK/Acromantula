package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.data.WorkspaceFileSystem
import java.io.File

class LocalWorkspaceClient(directory: File) : WorkspaceClient(File(directory, "db").toURI().toURL()) {

    /**
     * Directory where files of the workspace are stored
     */
    private val workspaceFileSystem = WorkspaceFileSystem(directory)
}