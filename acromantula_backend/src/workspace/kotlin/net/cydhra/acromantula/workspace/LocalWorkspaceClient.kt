package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.File

internal class LocalWorkspaceClient(directory: File) : WorkspaceClient(File(directory, "db").toURI().toURL()) {

    /**
     * Directory where files of the workspace are stored
     */
    private val workspaceFileSystem = WorkspaceFileSystem(directory, this.databaseClient)

    override fun uploadFile(fileEntity: FileEntity, content: ByteArray) {
        this.workspaceFileSystem.addResource(fileEntity, content)
    }
}