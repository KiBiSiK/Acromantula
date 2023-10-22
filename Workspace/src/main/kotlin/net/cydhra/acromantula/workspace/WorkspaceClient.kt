package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.database.DatabaseClient
import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.WorkspaceFileSystem
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.Channels

internal class WorkspaceClient(directory: File) {

    /**
     * A connection to the workspace database
     */
    internal val databaseClient = DatabaseClient(File(directory, "db").toURI().toURL())

    /**
     * Directory where files of the workspace are stored
     */
    private val workspaceFileSystem = WorkspaceFileSystem(directory, this.databaseClient)

    /**
     * Initialize client connections, resources, etc
     */
    fun initialize() {
        databaseClient.connect()
        workspaceFileSystem.initialize()
    }

    /**
     * Shutdown connections, release resources and terminate thread pools.
     */
    fun shutdown() {
        workspaceFileSystem.onShutdown()
    }

    /**
     * Migrate registered resources to new workspace client
     */
    fun migrateResources(newWorkspaceClient: WorkspaceClient) {
        this.workspaceFileSystem.migrate(newWorkspaceClient.workspaceFileSystem)
    }

    /**
     * Register a new type of archives at the database
     */
    fun registerArchiveType(fileTypeIdentifier: String) {
        this.workspaceFileSystem.registerArchiveType(fileTypeIdentifier)
    }

    /**
     * Upload a file into the workspace
     *
     * @param name of the file
     * @param parent database entry of the parent file, optional
     * @param content file binary content
     *
     * @return generated database entry
     */
    fun createFile(name: String, parent: FileEntity?, content: ByteArray): FileEntity {
        return this.workspaceFileSystem.createFile(name, parent, content)
    }

    /**
     * Update binary content of a file
     */
    fun updateFile(fileEntity: FileEntity, content: ByteArray) {
        this.workspaceFileSystem.updateResource(fileEntity, content)
    }

    /**
     * Rename a file. Does not move the file in the directory hierarchy.
     */
    fun renameFile(fileEntity: FileEntity, newName: String) {
        this.workspaceFileSystem.renameResource(fileEntity, newName)
    }

    /**
     * Download a file from the workspace. Returns the binary contents of the file as an input stream
     */
    fun downloadFile(fileEntity: FileEntity): InputStream {
        return this.workspaceFileSystem.openFile(fileEntity)
    }

    /**
     * Export a file into a given [outputStream]
     */
    fun exportFile(fileEntity: FileEntity, outputStream: OutputStream) {
        this.workspaceFileSystem.exportFile(fileEntity, Channels.newChannel(outputStream))
    }

    /**
     * Create a new directory in the workspace
     *
     * @param name directory name
     * @param parent directory parent. null if directory shall be created at top-level
     */
    fun createDirectory(name: String, parent: FileEntity?): FileEntity {
        return this.workspaceFileSystem.createDirectory(name, parent)
    }

    /**
     * Upload file view data into the workspace
     *
     * @param file reference file for the representation data
     * @param type view type
     * @param viewData the binary data of the file's view
     */
    fun createFileView(file: FileEntity, type: String, viewData: ByteArray): FileViewEntity {
        return this.workspaceFileSystem.createFileRepresentation(file, type, viewData)
    }

    /**
     *  Download a file view from the workspace. Returns the binary contents of the representation as an
     *  input stream
     */
    fun downloadFileView(fileView: FileViewEntity): InputStream {
        return this.workspaceFileSystem.openFileRepresentation(fileView)
    }

    /**
     * Delete File from workspace and database
     */
    fun deleteFile(fileEntity: FileEntity) {
        this.workspaceFileSystem.deleteFile(fileEntity)
    }

    /**
     * Move a file to another directory. If the file is a directory, all contents will be moved as well
     *
     * @param file file or directory to move
     * @param targetDirectory target directory or null, if file is to be moved to workspace root
     */
    fun moveFile(file: FileEntity, targetDirectory: FileEntity?) {
        this.workspaceFileSystem.moveResource(file, targetDirectory)
    }

    /**
     * Mark a directory as an archive of the given type
     */
    fun markAsArchive(directory: FileEntity, type: String) {
        this.workspaceFileSystem.markAsArchive(directory, type)
    }

    /**
     * Get the size of a file without reading the file
     */
    fun getFileSize(fileEntity: FileEntity): Long {
        return this.workspaceFileSystem.getResourceSize(fileEntity)
    }

    /**
     * Get the size of a file representation without reading its resource
     */
    fun getRepresentationSize(fileView: FileViewEntity): Long {
        return this.workspaceFileSystem.getFileRepresentationSize(fileView)
    }

    /**
     * Get a list of all top-level files and directories in the workspace
     */
    fun listFiles(): List<FileEntity> {
        return this.workspaceFileSystem.listFiles()
    }

    /**
     * Get a file by its path
     */
    fun queryPath(path: String): FileEntity {
        return this.workspaceFileSystem.queryPath(path)
    }

    /**
     * Get a file by its resource id
     */
    fun queryFile(id: Int): FileEntity {
        return this.workspaceFileSystem.queryFile(id)
    }

    fun listDirectory(fileEntity: FileEntity): List<FileEntity> {
        return this.workspaceFileSystem.listDirectory(fileEntity)
    }
}