package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.WorkspaceFileSystem
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.Channels

internal class LocalWorkspaceClient(directory: File) : WorkspaceClient(File(directory, "db").toURI().toURL()) {

    /**
     * Directory where files of the workspace are stored
     */
    private val workspaceFileSystem = WorkspaceFileSystem(directory, this.databaseClient)

    override fun registerArchiveType(fileTypeIdentifier: String) {
        this.workspaceFileSystem.registerArchiveType(fileTypeIdentifier)
    }

    override fun createFile(name: String, parent: FileEntity?, content: ByteArray): FileEntity {
        return this.workspaceFileSystem.createFile(name, parent, content)
    }

    override fun updateFile(fileEntity: FileEntity, content: ByteArray) {
        this.workspaceFileSystem.updateResource(fileEntity, content)
    }

    override fun renameFile(fileEntity: FileEntity, newName: String) {
        this.workspaceFileSystem.renameResource(fileEntity, newName)
    }

    override fun downloadFile(fileEntity: FileEntity): InputStream {
        return this.workspaceFileSystem.openFile(fileEntity)
    }

    override fun exportFile(fileEntity: FileEntity, outputStream: OutputStream) {
        this.workspaceFileSystem.exportFile(fileEntity, Channels.newChannel(outputStream))
    }

    override fun createDirectory(name: String, parent: FileEntity?): FileEntity {
        return this.workspaceFileSystem.createDirectory(name, parent)
    }

    override fun createFileView(file: FileEntity, type: String, viewData: ByteArray): FileViewEntity {
        return this.workspaceFileSystem.createFileRepresentation(file, type, viewData)
    }

    override fun downloadFileView(fileView: FileViewEntity): InputStream {
        return this.workspaceFileSystem.openFileRepresentation(fileView)
    }

    override fun deleteFile(fileEntity: FileEntity) {
        this.workspaceFileSystem.deleteFile(fileEntity)
    }

    override fun moveFile(file: FileEntity, targetDirectory: FileEntity?) {
        this.workspaceFileSystem.moveResource(file, targetDirectory)
    }

    override fun markAsArchive(directory: FileEntity, type: String) {
        this.workspaceFileSystem.markAsArchive(directory, type)
    }

    override fun getFileSize(fileEntity: FileEntity): Long {
        return this.workspaceFileSystem.getResourceSize(fileEntity)
    }

    override fun getRepresentationSize(fileView: FileViewEntity): Long {
        return this.workspaceFileSystem.getFileRepresentationSize(fileView)
    }
}