package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * An SQL query to list a directory tree relative to a starting directory selected by `%clause`
 */
private const val RECURSIVE_LIST_FILE_TREE_QUERY = """
    WITH RECURSIVE tree (id, name, parent, is_directory, type, resource, archive, path) AS 
    (
      SELECT id, name, parent, is_directory, type, resource, archive, CAST (id AS VARCHAR) As path
      FROM TreeFile
      WHERE %{clause}
      UNION ALL
        SELECT tf.id, tf.name, tf.parent, tf.is_directory, tf.type, tf.resource, tf.archive,
         (r.path || '.' || CAST  (tf.id AS VARCHAR)) AS path
        FROM TreeFile AS tf
          INNER JOIN tree AS r
          ON tf.parent = r.id
    )
    SELECT id, name, parent, is_directory, type, resource, archive FROM tree
    ORDER BY path
"""

/**
 * A `%clause` variant for [RECURSIVE_LIST_FILE_TREE_QUERY] for the workspace root
 */
private val FILE_TREE_QUERY_ROOT_CLAUSE = "parent IS NULL"

/**
 * A `%clause` variant for [RECURSIVE_LIST_FILE_TREE_QUERY] for a specified directory id as parent. The id is
 * inserted via prepared statement parameter
 */
private val FILE_TREE_QUERY_RELATIVE_CLAUSE = "parent = (?)"

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
        workspaceClient = LocalWorkspaceClient(File(".tmp"))
        workspaceClient.initialize()
    }

    fun onShutdown() {
        this.workspaceClient.shutdown()
    }

    fun loadNewWorkspace(workspaceFile: File) {
        logger.info("attempting to load new workspace. Shutting down current workspace...")
        workspaceClient.shutdown()

        logger.info("loading new workspace...")
        workspaceClient = LocalWorkspaceClient(workspaceFile)

        logger.info("initializing new workspace...")
        workspaceClient.initialize()

        logger.info("adding plugin specific database relations...")
        this.databaseInitializers.forEach { stmt ->
            transaction(statement = stmt)
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
        transaction(statement = block)
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
     * @param type representation type identifier
     * @param viewData binary data of the representation
     */
    fun addFileRepresentation(file: FileEntity, type: String, viewData: ByteArray): FileViewEntity {
        return workspaceClient.createFileView(file, type, viewData)
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
        TODO("not implemented")
//        val folderPath = path.removeSuffix("/").removePrefix("/").split('/')
//
//        return this.workspaceClient.databaseClient.transaction {
//            var results = FileEntity.find { FileTable.name eq folderPath.last() }.toList()
//            var currentFolderIndex = folderPath.lastIndex - 1
//
//            do {
//                when {
//                    results.isEmpty() -> error("file with path $path does not exist")
//                    results.count() == 1 -> return@transaction results.first()
//                    currentFolderIndex > -1 -> {
//                        results = results.filter { it.parent?.name?.equals(folderPath[currentFolderIndex]) == true }
//                        currentFolderIndex--
//                    }
//
//                    else -> error("the specified path was not unique")
//                }
//            } while (currentFolderIndex > -1)
//
//            error("the specified path was not unique")
//        }
    }

    /**
     * Get a [FileEntity] instance by a file id.
     */
    fun queryPath(id: Int): FileEntity {
        TODO("not implemented")
//        return this.workspaceClient.databaseClient.transaction {
//            FileEntity.find { FileTable.id eq id }.firstOrNull()
//                ?: error("file with id $id does not exist")
//        }
    }

    /**
     * Query a representation of a file. If it does exist, its entity is returned.
     *
     * @param fileEntity reference file for the representation
     * @param viewType type of representation in question
     *
     * @return a [FileViewEntity] instance if the view already exists, `null` otherwise
     */
    fun queryRepresentation(fileEntity: FileEntity, viewType: String): FileViewEntity? {
        TODO("not implemented")
//        return this.workspaceClient.databaseClient.transaction {
//            FileView.find {
//                FileViewTable.file eq fileEntity.id and
//                        (FileViewTable.type eq viewType)
//            }.firstOrNull()
//        }
    }

    /**
     * Recursively list files beginning with a root directory in a tree structure. If the root directory is null, the
     * repository root is used.
     */
    fun listFiles(): List<FileEntity> {
        return workspaceClient.listFiles()
    }

    fun getDirectoryContent(directory: FileEntity?): List<FileEntity> {
        TODO("not implemented")
//        return workspaceClient.databaseClient.transaction {
//            val content = if (directory == null) {
//                FileEntity.find { FileTable.parent.isNull() }
//            } else {
//                FileEntity.find { FileTable.parent eq directory.id }
//            }
//
//            content.toList()
//        }
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
        workspaceClient.deleteFile(fileEntity)
    }

    /**
     * Register an archive type at the database
     */
    fun registerArchiveType(fileTypeIdentifier: String) {
        // make sure we re-register it when a new workspace is loaded
        registerAtDatabase {
            workspaceClient.registerArchiveType(fileTypeIdentifier)
        }
    }
}