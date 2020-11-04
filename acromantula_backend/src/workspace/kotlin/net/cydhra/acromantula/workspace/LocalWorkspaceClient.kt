package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.Channels

internal class LocalWorkspaceClient(directory: File) : WorkspaceClient(File(directory, "db").toURI().toURL()) {

    /**
     * Directory where files of the workspace are stored
     */
    private val workspaceFileSystem = WorkspaceFileSystem(directory, this.databaseClient)

    override fun uploadFile(fileEntity: FileEntity, content: ByteArray) {
        this.workspaceFileSystem.addResource(fileEntity, content)
    }

    override fun downloadFile(fileEntity: FileEntity): InputStream {
        return this.workspaceFileSystem.openResource(fileEntity)
    }

    override fun exportFile(fileEntity: FileEntity, outputStream: OutputStream) {
        this.workspaceFileSystem.exportResource(fileEntity, Channels.newChannel(outputStream))
    }
}