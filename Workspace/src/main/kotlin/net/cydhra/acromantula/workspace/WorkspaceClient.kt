package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.database.DatabaseClient
import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.InputStream
import java.io.OutputStream
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
     * Initialize client connections, resources, etc
     */
    open fun initialize() {
        databaseClient.connect()
    }

    /**
     * Shutdown connections, release resources and terminate thread pools.
     */
    open fun shutdown() {

    }

    /**
     * Register a new type of archives at the database
     */
    abstract fun registerArchiveType(fileTypeIdentifier: String)

    /**
     * Upload a file into the workspace
     *
     * @param fileEntity database entry to reference the file
     * @param content file binary content
     */
    abstract fun uploadFile(fileEntity: FileEntity, content: ByteArray)

    /**
     * Update binary content of a file
     */
    abstract fun updateFile(fileEntity: FileEntity, content: ByteArray)

    /**
     * Rename a file. Does not move the file in the directory hierarchy.
     */
    abstract fun renameFile(fileEntity: FileEntity, newName: String)

    /**
     * Download a file from the workspace. Returns the binary contents of the file as an input stream
     */
    abstract fun downloadFile(fileEntity: FileEntity): InputStream

    /**
     * Export a file into a given [outputStream]
     */
    abstract fun exportFile(fileEntity: FileEntity, outputStream: OutputStream)

    /**
     * Upload file representation data into the workspace
     *
     * @param file reference file for the representation data
     * @param type representation type
     * @param viewData the binary data of the file's representation
     */
    abstract fun uploadFileRepresentation(file: FileEntity, type: String, viewData: ByteArray)

    /**
     *  Download a file representation from the workspace. Returns the binary contents of the representation as an
     *  input stream
     */
    abstract fun downloadRepresentation(representation: FileRepresentation): InputStream

    /**
     * Get a URL that grants direct file access onto a file in workspace
     *
     * @param fileEntity the resource id
     *
     * @return a [URL] pointing to the file
     */
    abstract fun getFileUrl(fileEntity: Int): URL

    /**
     * Delete File from workspace and database
     */
    abstract fun deleteFile(fileEntity: FileEntity)

    /**
     * Move a file to another directory. If the file is a directory, all contents will be moved as well
     *
     * @param file file or directory to move
     * @param targetDirectory target directory or null, if file is to be moved to workspace root
     */
    abstract fun moveFile(file: FileEntity, targetDirectory: FileEntity?)

    /**
     * Mark a directory as an archive of the given type
     */
    abstract fun markAsArchive(directory: FileEntity, type: String)

    /**
     * Get the size of a file without reading the file
     */
    abstract fun getFileSize(fileEntity: FileEntity): Long

    /**
     * Get the size of a file representation without reading its resource
     */
    abstract fun getRepresentationSize(fileRepresentation: FileRepresentation): Long
}