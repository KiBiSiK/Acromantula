package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.disassembly.FileRepresentationTable
import net.cydhra.acromantula.workspace.filesystem.ArchiveEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import net.cydhra.acromantula.workspace.util.TreeNode
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.lookup.StrSubstitutor
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.sql.ResultSet
import java.util.*

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
     * Add an archive entry into the workspace file tree. Since the archive is only parent to its content, no actual
     * data is associated with it. It is simply a [FileEntity] that has an [ArchiveEntity] associated
     *
     * @param archiveName simple name of the archive
     */
    fun addArchiveEntry(archiveName: String, parent: FileEntity?): FileEntity {
        logger.trace("creating archive entry in file tree: \"$archiveName\"")
        return workspaceClient.databaseClient.transaction {
            val archive = ArchiveEntity.new {}

            FileEntity.new {
                this.name = archiveName
                this.parent = parent
                this.isDirectory = true
                this.archiveEntity = archive
            }
        }
    }

    /**
     * Add a directory entry into the workspace file tree.
     */
    fun addDirectoryEntry(name: String, parent: FileEntity?): FileEntity {
        logger.trace("creating directory entry in file tree: \"$name\"")
        return workspaceClient.databaseClient.transaction {
            FileEntity.new {
                this.name = name
                this.parent = parent
                this.isDirectory = true
            }
        }
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
        val fileEntity = workspaceClient.databaseClient.transaction {
            FileEntity.new {
                this.name = name
                this.parent = parent
            }
        }

        workspaceClient.uploadFile(fileEntity, content)
        return fileEntity
    }

    /**
     * Rename a file in workspace. This method cannot move the file.
     *
     * @param name unique file path suffix (see [queryPath])
     * @param newName new file name (without the path, only the name)
     */
    fun renameFileEntry(name: String, newName: String) {
        val fileEntity = queryPath(name)
        this.workspaceClient.renameFile(fileEntity, newName)
    }

    /**
     * Upload the binary data of a file representation into the workspace to cache it for later access. It will be
     * automatically deleted when the reference file changes //TODO
     *
     * @param file reference file for the representation data
     * @param type representation type identifier
     * @param viewData binary data of the representation
     */
    fun addFileRepresentation(file: FileEntity, type: String, viewData: ByteArray): FileRepresentation {
        return workspaceClient.databaseClient.transaction {
            file.refresh()
            logger.trace("creating file view for file: \"${file.name}\"")

            workspaceClient.uploadFileRepresentation(file, type, viewData)
            FileRepresentation
                .find { FileRepresentationTable.file eq file.id and (FileRepresentationTable.type eq type) }
                .single()
        }
    }

    /**
     * Update content of a file with new content
     */
    fun updateFileEntry(fileEntity: FileEntity, byteArray: ByteArray) {
        workspaceClient.databaseClient.transaction {
            fileEntity.refresh()
            logger.trace("updating file content for: \"${fileEntity.name}\"")
            workspaceClient.updateFile(fileEntity, byteArray)
        }
    }

    /**
     * Get a [FileEntity] instance by a unique path in workspace. The path does not need to be complete, it only
     * needs to be a unique path suffix. For example, if two paths exist in the workspace `root/example_1/a` and
     * `root/example_2/a` then `a` is insufficient, but `example_1/a` is sufficient to identify the file. The path
     * separator is `/`
     */
    fun queryPath(path: String): FileEntity {
        val folderPath = path.removeSuffix("/").removePrefix("/").split('/')

        return this.workspaceClient.databaseClient.transaction {
            var results = FileEntity.find { FileTable.name eq folderPath.last() }.toList()
            var currentFolderIndex = folderPath.lastIndex - 1

            do {
                when {
                    results.isEmpty() -> error("file with path $path does not exist")
                    results.count() == 1 -> return@transaction results.first()
                    currentFolderIndex > -1 -> {
                        results = results.filter { it.parent?.name?.equals(folderPath[currentFolderIndex]) == true }
                        currentFolderIndex--
                    }

                    else -> error("the specified path was not unique")
                }
            } while (currentFolderIndex > -1)

            error("the specified path was not unique")
        }
    }

    /**
     * Get a [FileEntity] instance by a file id.
     */
    fun queryPath(id: Int): FileEntity {
        return this.workspaceClient.databaseClient.transaction {
            FileEntity.find { FileTable.id eq id }.firstOrNull()
                ?: error("file with id $id does not exist")
        }
    }

    /**
     * Query a representation of a file. If it does exist, its entity is returned.
     *
     * @param fileEntity reference file for the representation
     * @param viewType type of representation in question
     *
     * @return a [FileRepresentation] instance if the view already exists, `null` otherwise
     */
    fun queryRepresentation(fileEntity: FileEntity, viewType: String): FileRepresentation? {
        return this.workspaceClient.databaseClient.transaction {
            FileRepresentation.find {
                FileRepresentationTable.file eq fileEntity.id and
                        (FileRepresentationTable.type eq viewType)
            }.firstOrNull()
        }
    }

    fun <T : Any> String.execAndMap(transform: (ResultSet) -> T): List<T> {
        val result = arrayListOf<T>()
        TransactionManager.current().exec(this) { rs ->
            while (rs.next()) {
                result += transform(rs)
            }
        }
        return result
    }

    /**
     * Recursively list files beginning with a root directory in a tree structure. If the root directory is null, the
     * repository root is used.
     */
    fun listFilesRecursively(root: FileEntity? = null): List<TreeNode<FileEntity>> {
        // obtain the entire file tree from database
        return this.workspaceClient.databaseClient.transaction transaction@{
            val con = TransactionManager.current().connection

            val query = if (root == null) {
                val substitutor = StrSubstitutor(mapOf("clause" to FILE_TREE_QUERY_ROOT_CLAUSE))
                    .setVariablePrefix("%{")
                    .setVariableSuffix("}")
                substitutor.replace(RECURSIVE_LIST_FILE_TREE_QUERY)
            } else {
                val substitutor = StrSubstitutor(
                    mapOf(
                        "clause" to FILE_TREE_QUERY_RELATIVE_CLAUSE
                            .replace("?", root.id.value.toString())
                    )
                )
                    .setVariablePrefix("%{")
                    .setVariableSuffix("}")
                substitutor.replace(RECURSIVE_LIST_FILE_TREE_QUERY)
            }

            return@transaction TransactionManager.current()
                .exec(object : Statement<List<TreeNode<FileEntity>>>(StatementType.SELECT, emptyList()) {
                    override fun PreparedStatementApi.executeInternal(transaction: Transaction): List<TreeNode<FileEntity>> {
                        val result = executeQuery()
                        return result.let resultHandler@{ rs ->
                            rs.use { rs ->
                                // linearly construct the result list from the query result
                                val rootNodes = mutableListOf<TreeNode<FileEntity>>()
                                val parentStack = Stack<TreeNode<FileEntity>>()
                                var lastElement: TreeNode<FileEntity>

                                if (rs.next()) {
                                    val firstElement = TreeNode(
                                        FileEntity.wrapRow(
                                            ResultRow.create(
                                                rs, listOf(
                                                    FileTable.id,
                                                    FileTable.name,
                                                    FileTable.parent,
                                                    FileTable.isDirectory,
                                                    FileTable.type,
                                                    FileTable.archive,
                                                ).distinct().mapIndexed { index, field -> field to index }.toMap()
                                            )
                                        )
                                    )
                                    lastElement = firstElement
                                    parentStack.push(firstElement)
                                    rootNodes.add(firstElement)
                                } else {
                                    return@resultHandler emptyList()
                                }

                                while (rs.next()) {
                                    val currentElement = TreeNode(
                                        FileEntity.wrapRow(
                                            ResultRow.create(
                                                rs, listOf(
                                                    FileTable.id,
                                                    FileTable.name,
                                                    FileTable.parent,
                                                    FileTable.isDirectory,
                                                    FileTable.type,
                                                    FileTable.archive,
                                                ).distinct().mapIndexed { index, field -> field to index }.toMap()
                                            )
                                        )
                                    )

                                    if (currentElement.value.parent == lastElement.value) {
                                        parentStack.push(lastElement)
                                        lastElement.appendChild(currentElement)
                                    } else if (currentElement.value.parent == parentStack.peek().value) {
                                        parentStack.peek().appendChild(currentElement)
                                    } else {
                                        while (true) {
                                            parentStack.pop()

                                            if (parentStack.isNotEmpty()) {
                                                if (currentElement.value.parent == parentStack.peek().value) {
                                                    parentStack.peek().appendChild(currentElement)
                                                    break
                                                }
                                            } else {
                                                break
                                            }
                                        }

                                        if (parentStack.isEmpty()) {
                                            rootNodes.add(currentElement)
                                            parentStack.push(currentElement)
                                        }
                                    }

                                    lastElement = currentElement
                                }

                                rootNodes
                            }
                        }
                    }

                    override fun prepareSQL(transaction: Transaction): String = query

                    override fun arguments(): Iterable<Iterable<Pair<IColumnType, Any?>>> = emptyList()
                }) ?: error("query did not produce result set")
        }
    }

    fun getDirectoryContent(directory: FileEntity?): List<FileEntity> {
        return workspaceClient.databaseClient.transaction {
            val content = if (directory == null) {
                FileEntity.find { FileTable.parent.isNull() }
            } else {
                FileEntity.find { FileTable.parent eq directory.id }
            }

            content.toList()
        }
    }

    /**
     * A debug function to directly execute a raw, unprepared SQL query on the workspace database. This function
     * should not be called in production builds, but is only meant for debugging the database from the CLI
     */
    fun directQuery(query: String): List<List<String>> {
        return this.workspaceClient.databaseClient.directQuery(query)
    }

    /**
     * Get an [InputStream] of the file contents of the given [fileEntity]
     */
    fun getFileContent(fileEntity: FileEntity): InputStream {
        return this.workspaceClient.downloadFile(fileEntity)
    }

    /**
     * Get an [InputStream] that contains a file representation as binary data.
     */
    fun getRepresentationContent(representation: FileRepresentation): InputStream {
        return this.workspaceClient.downloadRepresentation(representation)
    }

    /**
     * Get an URL pointing to the file system address of a file. This is mainly used to instruct the front-end where
     * to find something and should not be used by the back-end for direct file manipulation
     *
     * @param resource the resource id assigned by the workspace
     *
     * @return an URL pointing to the file on the filesystem
     */
    fun getFileUrl(resource: Int): URL {
        return this.workspaceClient.getFileUrl(resource)
    }

    /**
     * Export a file into the given output stream
     */
    fun exportFile(fileEntity: FileEntity, outputStream: OutputStream) {
        this.workspaceClient.exportFile(fileEntity, outputStream)
    }
}