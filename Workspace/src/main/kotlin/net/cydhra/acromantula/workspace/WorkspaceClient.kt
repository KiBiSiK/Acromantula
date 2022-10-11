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

}