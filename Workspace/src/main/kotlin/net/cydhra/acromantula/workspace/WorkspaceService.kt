package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.disassembly.MediaType
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Transaction
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Facade service for the workspace sub-system. Everything related to data storage and data operation is delegated
 * from here.
 */
object WorkspaceService {
    private val logger = LogManager.getLogger()

    /**
     * The client of the current workspace connection.
     */
    private lateinit var workspaceClient: WorkspaceClient

    private val databaseInitializers = mutableListOf<Transaction.() -> Unit>()

    /**
     * Called upon application startup. Load default workspace and subscribe to events if necessary.
     */
    fun initialize() {
        workspaceClient = WorkspaceClient(File(".tmp"))
        workspaceClient.initialize()
    }

    fun onShutdown() {
        this.workspaceClient.shutdown()
    }

    fun loadNewWorkspace(workspaceFile: File) {
        logger.info("attempting to load new workspace. Shutting down current workspace...")
        workspaceClient.shutdown()

        logger.info("loading new workspace...")
        val newWorkspaceClient = WorkspaceClient(workspaceFile)

        logger.info("initializing new workspace...")
        newWorkspaceClient.initialize()

        logger.info("migrating plugin resources to new workspace...")
        this.workspaceClient.migrateResources(newWorkspaceClient)

        this.workspaceClient = newWorkspaceClient

        logger.info("adding plugin specific database relations to new workspace...")
        this.databaseInitializers.forEach { stmt ->
            this.workspaceClient.databaseClient.transaction(statement = stmt)
        }
    }

    /**
     * Register new tables and similar SQL constructs at the workspace database. Those will be automatically applied
     * to any database that is opened as part of a workspace. This is intended for plugins to add their own tables to
     * the workspace.
     */
    fun registerAtDatabase(block: Transaction.() -> Unit) {
        databaseInitializers += block

        // plugins are initialized after the workspace, so the initial database has to be updated as well
        this.workspaceClient.databaseClient.transaction(statement = block)
    }

    /**
     * Mark a directory entry as an archive of the given type
     *
     * @param directory directory to be marked as an archive
     * @param type archive type identifier
     */
    fun addArchiveEntry(directory: FileEntity, type: String) {
        require(directory.isDirectory) { "cannot mark files as archives" }

        logger.trace("creating archive entry in file tree: \"${directory.name}\"")
        workspaceClient.markAsArchive(directory, type)
    }

    /**
     * Add a directory entry into the workspace file tree.
     */
    fun addDirectoryEntry(name: String, parent: FileEntity?): FileEntity {
        logger.trace("creating directory entry in file tree: \"$name\"")
        return workspaceClient.createDirectory(name, parent)
    }

    /**
     * Add a file into the workspace. An entry in database is create for reference and the content is uploaded into
     * the workspace.
     *
     * @param name file name
     * @param parent optional parent directory
     * @param content file binary content
     */
    fun addFileEntry(name: String, parent: FileEntity?, content: ByteArray): FileEntity {
        logger.trace("creating file entry in file tree: \"$name\"")
        return workspaceClient.createFile(name, parent, content)
    }

    /**
     * Rename a file in workspace. This method cannot move the file.
     *
     * @param fileEntity file to be renamed
     * @param newName new file name (without the path, only the name)
     */
    fun renameFileEntry(fileEntity: FileEntity, newName: String) {
        this.workspaceClient.renameFile(fileEntity, newName)
    }

    /**
     * Upload the binary data of a file representation into the workspace to cache it for later access. It will be
     * automatically deleted when the reference file changes
     *
     * @param file reference file for the representation data
     * @param generatorType representation type identifier
     * @param viewData binary data of the representation
     */
    fun addFileRepresentation(file: FileEntity, generatorType: String, mediaType: MediaType, viewData: ByteArray):
            FileViewEntity {
        return workspaceClient.createFileView(file, generatorType, mediaType, viewData)
    }

    /**
     * Update content of a file with new content
     */
    fun updateFileContent(fileEntity: FileEntity, byteArray: ByteArray) {
        workspaceClient.updateFile(fileEntity, byteArray)
    }

    /**
     * Get a [FileEntity] instance by a unique path in workspace. The path does not need to be complete, it only
     * needs to be a unique path suffix. For example, if two paths exist in the workspace `root/example_1/a` and
     * `root/example_2/a` then `a` is insufficient, but `example_1/a` is sufficient to identify the file. The path
     * separator is `/`
     */
    fun queryPath(path: String): FileEntity {
        return workspaceClient.queryPath(path)
    }

    /**
     * Get a [FileEntity] instance by a file id.
     */
    fun queryPath(id: Int): FileEntity {
        return workspaceClient.queryFile(id)
    }

    /**
     * Recursively list files beginning with a root directory in a tree structure. If the root directory is null, the
     * repository root is used.
     */
    fun listFiles(): List<FileEntity> {
        return workspaceClient.listFiles()
    }

    fun listDirectory(fileEntity: FileEntity): List<FileEntity> {
        require(fileEntity.isDirectory) { "can only list files in directories" }

        return workspaceClient.listDirectory(fileEntity)
    }

    /**
     * A debug function to directly execute a raw, unprepared SQL query on the workspace database. This function
     * should not be called in production builds, but is only meant for debugging the database from the CLI
     */
    fun directQuery(query: String): List<List<String>> {
        return this.workspaceClient.databaseClient.directQuery(query)
    }

    fun getFileSize(fileEntity: FileEntity): Long {
        return this.workspaceClient.getFileSize(fileEntity)
    }

    /**
     * Get an [InputStream] of the file contents of the given [fileEntity]
     */
    fun getFileContent(fileEntity: FileEntity): InputStream {
        return this.workspaceClient.downloadFile(fileEntity)
    }

    fun getRepresentationSize(fileView: FileViewEntity): Long {
        return workspaceClient.getRepresentationSize(fileView)
    }

    /**
     * Get an [InputStream] that contains a file representation as binary data.
     */
    fun getRepresentationContent(representation: FileViewEntity): InputStream {
        return this.workspaceClient.downloadFileView(representation)
    }

    /**
     * Export a file into the given output stream
     */
    fun exportFile(fileEntity: FileEntity, outputStream: OutputStream) {
        this.workspaceClient.exportFile(fileEntity, outputStream)
    }

    fun moveFileEntry(file: FileEntity, targetDirectory: FileEntity?) {
        require(targetDirectory?.isDirectory ?: true) { "target must be a directory or null" }

        this.workspaceClient.moveFile(file, targetDirectory)
    }

    /**
     * Delete a file or directory, and its associated resource from the workspace. All directory contents will be
     * deleted as well.
     */
    fun deleteFileEntry(fileEntity: FileEntity) {
        if (fileEntity.isDirectory) {
            // use secondary list because deleting files will modify backing collection
            val children = fileEntity.children.toList()
            children.forEach(::deleteFileEntry)
        }

        workspaceClient.deleteFile(fileEntity)
    }

    /**
     * Register an archive type at the database
     */
    fun registerArchiveType(fileTypeIdentifier: String) {
        workspaceClient.registerArchiveType(fileTypeIdentifier)
    }

    /**
     * Perform a database transaction in the current workspace database
     */
    fun <T> databaseTransaction(transaction: Transaction.() -> T): T {
        return this.workspaceClient.databaseClient.transaction(transaction)
    }
}