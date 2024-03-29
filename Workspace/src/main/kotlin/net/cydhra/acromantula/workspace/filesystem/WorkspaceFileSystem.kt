package net.cydhra.acromantula.workspace.filesystem

import com.google.gson.GsonBuilder
import net.cydhra.acromantula.workspace.database.DatabaseClient
import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.disassembly.FileViewTable
import net.cydhra.acromantula.workspace.disassembly.MediaType
import net.cydhra.acromantula.workspace.filesystem.ArchiveTable.entityId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.WritableByteChannel
import java.time.Instant

/**
 * A facade to all file system interaction of a workspace. No other part within the workspace should directly
 * interact with files of the workspace.
 */
// TODO somehow handle exclusive write access to resources, so no two clients ever write the same resource at once
internal class WorkspaceFileSystem(workspacePath: File, private val databaseClient: DatabaseClient) {

    /**
     * A file containing meta information for this service to correctly operate
     */
    private val indexFile = File(workspacePath, "index")

    /**
     * The directory of a workspace where all resources are created and managed
     */
    private val resourceDirectory = File(workspacePath, "resources")

    /**
     * Index with meta information required for file system operation of the workspace
     */
    private val index: WorkspaceIndex

    /**
     * Serializer for workspace index
     */
    private val gson = GsonBuilder().create()

    /**
     * Registered archive types
     */
    private val archiveTypeIdentifiers = mutableMapOf<String, Int>()

    /**
     * Registered archive identifiers to archive name mapping
     */
    private val archiveTypeNames = mutableMapOf<Int, String>()

    /**
     * Event broker for file system events
     */
    private val eventBroker = FileSystemEventBroker()

    /**
     * Listener for [eventBroker] that will sync all file system events into the backing database
     */
    private val fileSystemDatabaseSync = FileSystemDatabaseSync(this, databaseClient)

    /**
     * All file trees in the workspace. This list guarantees that only one file entity exists per file, and no two
     * concurrent file entities for the same file exist and get out of sync.
     */
    private val fileTrees = mutableListOf<FileEntity>()

    /**
     * Mapping of file resource ids to file entity
     */
    private val fileResourceMapping = mutableMapOf<Int, FileEntity>()

    init {
        if (!resourceDirectory.exists()) resourceDirectory.mkdirs()

        if (!indexFile.exists()) {
            index = WorkspaceIndex()
            saveIndex()
        } else {
            index = gson.fromJson(FileReader(indexFile), WorkspaceIndex::class.java)
        }

        eventBroker.registerObserver(fileSystemDatabaseSync)
    }

    fun initialize() {
        databaseClient.transaction {
            val fileIdMapping = mutableMapOf<Int, FileEntity>()

            fun convert(row: ResultRow): FileEntity {
                return FileEntity(
                    row[FileTable.name],
                    if (row[FileTable.parent] != null) {
                        fileIdMapping[row[FileTable.parent]!!.value]!!
                    } else {
                        null
                    },
                    row[FileTable.isDirectory],
                    row[FileTable.type],
                    row[FileTable.archive]?.value?.let { archiveTypeNames[it] },
                    row[FileTable.resource]
                ).apply {
                    this.databaseId = row[FileTable.id]
                }
            }

            fun handleFiles(query: SqlExpressionBuilder.() -> Op<Boolean>) {
                for (row in FileTable.select(query)) {
                    val fileEntity = convert(row)
                    fileResourceMapping[fileEntity.resource] = fileEntity
                    fileIdMapping[fileEntity.databaseId.value] = fileEntity
                    if (fileEntity.parent == null) {
                        fileTrees.add(fileEntity)
                    } else {
                        fileEntity.parent!!.childEntities.add(fileEntity)
                    }

                    if (fileEntity.isDirectory) {
                        handleFiles { FileTable.parent eq fileEntity.databaseId }
                    }
                }
            }

            handleFiles { FileTable.parent.isNull() }

            fun convertView(row: ResultRow): FileViewEntity {
                return FileViewEntity(
                    file = fileIdMapping[row[FileViewTable.file].value]!!,
                    type = row[FileViewTable.viewGenerator],
                    mediaType = row[FileViewTable.mediaType],
                    resource = row[FileViewTable.resource],
                    created = Instant.ofEpochMilli(row[FileViewTable.created].millis)
                )
            }

            FileViewTable.selectAll().forEach { resultRow ->
                val view = convertView(resultRow)
                view.file.viewEntities.add(view)
            }
        }
    }

    /**
     * Shutdown the workspace file system and all its resources gracefully.
     */
    fun onShutdown() {
        this.eventBroker.shutdown()
        this.saveIndex()
    }

    /**
     * Migrate the current workspace to another workspace. This will not call [onShutdown] nor
     * [initialize] on the workspaces. Those methods should be called before this method.
     * It will migrate all registered resources to the new workspace.
     */
    fun migrate(other: WorkspaceFileSystem) {
        // unregister the sync observer that is specific to this instance
        this.eventBroker.unregisterObserver(fileSystemDatabaseSync)

        // register remaining observers at new event broker
        this.eventBroker.migrateObservers(other.eventBroker)

        // register archive types in new workspace
        this.archiveTypeIdentifiers.keys.forEach(other::registerArchiveType)
    }

    /**
     * Add a resource to the workspace and associate it with a new file entity.
     *
     * @param name name of the new file
     * @param parent database entity of the parent file. optional
     * @param content the resource's content in raw binary form
     *
     * @return a [FileEntity] handle to reference this file later
     */
    fun createFile(name: String, parent: FileEntity?, content: ByteArray): FileEntity {
        // create a new id for the file which will be its unique identifier in the file system
        val resourceIndex = index.getNextFileIndex()

        // create file and write content
        val newFile = File(this.resourceDirectory, resourceIndex.toString())
        newFile.writeBytes(content)

        // create file entity and add it to the file tree. If the file is not created at toplevel, add it to its
        // parent file
        val fileEntity = insertFileEntityIntoTree(name, parent, false, null, null, resourceIndex)

        // dispatch file creation event which will sync the file back to the database
        eventBroker.dispatch(FileSystemEvent.FileCreatedEvent(fileEntity))

        // save the file index
//        saveIndex() // TODO: dispatch this in an extra thread to not clog the file system with unreasonable writes
        return fileEntity
    }

    /**
     * Read the content of a resource from workspace.
     *
     * @param file the resource to read
     *
     * @return the raw binary content of the given resource as a direct buffer.
     *
     * @throws IllegalArgumentException if [file] is a directory
     */
    private fun readResource(file: FileEntity): ByteBuffer {
        val channel = this.openFile(file).channel
        return channel.use { it.map(FileChannel.MapMode.READ_ONLY, 0L, channel.size()) }
    }

    /**
     * Offer the content of a file as an [InputStream].
     *
     * @param file the resource to read
     *
     * @return an [InputStream] for the file
     *
     * @throws IllegalArgumentException if [file] entity is a directory.
     */
    fun openFile(file: FileEntity): FileInputStream {
        require(!file.isDirectory) { "cannot open file stream of directory" }
        return openResource(file.resource)
    }

    /**
     * Update the content of a resource from workspace.
     *
     * @param file the resource to update
     * @param newContent the new resource content
     *
     * @throws IllegalArgumentException if [file] is a directory.
     */
    fun updateResource(file: FileEntity, newContent: ByteArray) {
        require(!file.isDirectory) { "cannot open file stream of directory" }

        val channel = File(resourceDirectory, file.resource.toString()).apply { delete() }.apply { createNewFile() }
            .outputStream().channel

        val contentBuffer = ByteBuffer.wrap(newContent)

        while (contentBuffer.remaining() > 0) {
            channel.write(contentBuffer)
        }

        channel.close()

        // delete cached representations as they are now invalid.
        deleteCachedViews(file)

        eventBroker.dispatch(FileSystemEvent.FileUpdatedEvent(file))
    }

    /**
     * Rename a file or directory. This does not move the file, it only changes its own name.
     *
     * @param fileEntity file to rename
     * @param newName new file name without file path
     */
    fun renameResource(fileEntity: FileEntity, newName: String) {
        val oldName = fileEntity.name
        fileEntity.name = newName
        eventBroker.dispatch(FileSystemEvent.FileRenamedEvent(fileEntity, oldName))
    }

    /**
     * Delete a file's content from workspace.
     *
     * @param file the file to delete.
     *
     * @throws IllegalArgumentException if the file is a directory
     */
    fun deleteFile(file: FileEntity) {
        require(file.children.isEmpty()) { "cannot delete non-empty directory" }

        val backingResource = File(resourceDirectory, file.resource.toString())

        // remove from parent's children and unset parent, remove from file trees
        if (file.parent != null) {
            file.parent!!.childEntities.remove(file)
            file.parent = null
        } else {
            fileTrees.remove(file)
        }

        // delete backing resource, if one exists (none exist for directories)
        if (backingResource.exists()) backingResource.delete()

        // delete cached representations as they are now invalid
        deleteCachedViews(file)

        eventBroker.dispatch(FileSystemEvent.FileDeletedEvent(file))
    }


    /**
     * Move a file to a new location in the file tree.
     *
     * @param targetDirectory target directory or null if target is workspace root
     */
    fun moveResource(file: FileEntity, targetDirectory: FileEntity?) {
        val oldParent = file.parent
        if (file.parent != null) {
            file.parent!!.childEntities.remove(file)
            file.parent = targetDirectory
            targetDirectory?.childEntities?.add(file)
        } else {
            fileTrees.remove(file)
            file.parent = targetDirectory
            targetDirectory?.childEntities?.add(file)
        }

        if (targetDirectory == null) {
            fileTrees.add(file)
        }

        eventBroker.dispatch(FileSystemEvent.FileMovedEvent(file, oldParent))
    }

    /**
     * Export a resource from the workspace by copying it into a channel. The channel is not closed afterwards. The
     * resource is read from disk.
     *
     * @param file a file handle
     * @param output a byte channel to write the file content into
     *
     * @throws IllegalArgumentException if [file] is a directory
     */
    fun exportFile(file: FileEntity, output: WritableByteChannel) {
        val buffer = readResource(file)
        while (buffer.remaining() > 0) {
            output.write(buffer)
        }
    }

    /**
     * Create a new directory in the file tree.
     *
     * @param name name of the directory without path
     * @param parent optional parent directory. Null, if directory is to be created at workspace root
     */
    fun createDirectory(name: String, parent: FileEntity?): FileEntity {
        val resourceIndex = index.getNextFileIndex();
        val fileEntity = insertFileEntityIntoTree(name, parent, true, null, null, resourceIndex)

        // dispatch file creation event which will sync the file back to the database
        eventBroker.dispatch(FileSystemEvent.FileCreatedEvent(fileEntity))

        // save the file index
        saveIndex()
        return fileEntity
    }

    /**
     * Get the file size of a resource without reading the resource from disk
     *
     * @param file a file handle
     *
     * @throws IllegalArgumentException if [file] is a directory
     */
    fun getResourceSize(file: FileEntity): Long {
        require(!file.isDirectory) { "directories have no size" }
        return File(resourceDirectory, file.resource.toString()).length()
    }

    /**
     * Creates a file representation resource in workspace.
     *
     * @param file create view for this file
     * @param generatorType view generator name
     * @param content view resource content
     */
    fun createFileRepresentation(
        file: FileEntity,
        generatorType: String,
        mediaType: MediaType,
        content: ByteArray
    ): FileViewEntity {
        val resourceIndex = index.getNextFileIndex()
        val viewEntity = FileViewEntity(file, generatorType, mediaType, resourceIndex, Instant.now())

        val newFile = File(this.resourceDirectory, resourceIndex.toString()).apply { createNewFile() }
        newFile.writeBytes(content)

        eventBroker.dispatch(FileSystemEvent.ViewCreatedEvent(file, viewEntity))

        file.viewEntities.add(viewEntity)
        return viewEntity
    }

    /**
     * Open a file view content in an [InputStream]
     *
     * @param view a file view entity to read
     */
    fun openFileRepresentation(view: FileViewEntity): InputStream {
        return openResource(view.resource)
    }

    /**
     * Get the file size of a representation resource without reading the resource from disk
     */
    fun getFileRepresentationSize(representation: FileViewEntity): Long {
        return File(resourceDirectory, representation.resource.toString()).length()
    }

    /**
     * Register an archive type by its identifier. The identifier is expected to be unique. If it is already present
     * in the database, the existing id will be reused. This means registering an archive twice will not fail
     */
    fun registerArchiveType(fileTypeIdentifier: String) {
        transaction {
            archiveTypeIdentifiers.put(
                fileTypeIdentifier, ArchiveTable.insertAndGetId {
                    it[typeIdent] = fileTypeIdentifier
                }.value
            )
        }
    }

    /**
     * Mark a directory as an archive using the archive type identifier
     *
     * @param directory directory handle
     * @param type archive type name
     *
     * @throws IllegalArgumentException if [directory] is not a directory handle
     */
    fun markAsArchive(directory: FileEntity, type: String) {
        require(directory.isDirectory) { "can only mark directories as archives" }
        directory.archiveType = type
        eventBroker.dispatch(FileSystemEvent.ArchiveCreatedEvent(directory))
    }

    /**
     * Get a list of top-level files in the workspace
     */
    fun listFiles(): List<FileEntity> {
        return this.fileTrees
    }

    /**
     * Query a file or directory by its path. Returns the specified file.
     *
     * @throws IllegalStateException if one of the file entities within the path (but the last) is not a directory
     * @throws IllegalStateException if one of the files in the path does not exist
     */
    fun queryPath(path: String): FileEntity {
        val folderPath = path.removeSuffix("/").removePrefix("/").split('/')

        var currentDirectory: List<FileEntity> = fileTrees
        var currentPathIndex = 0

        do {
            val child = currentDirectory.find { it.name.removeSuffix("/") == folderPath[currentPathIndex] }
            if (child != null) {
                if (currentPathIndex + 1 == folderPath.size) {
                    return child
                } else {
                    if (child.isDirectory) {
                        currentDirectory = child.children
                    } else {
                        error("${child.name} is not a directory")
                    }
                }
            } else {
                error(
                    "${folderPath[currentPathIndex]} not found in ${
                        currentDirectory.joinToString(
                            ", ", transform = FileEntity::name, prefix = "[", postfix = "]"
                        )
                    }"
                )
            }
            currentPathIndex++
        } while (true)
    }

    /**
     * Get a file by its resource id
     */
    fun queryFile(id: Int): FileEntity {
        return fileResourceMapping[id] ?: error("resource id $id does not exist")
    }

    /**
     * Get the internal id representing an archive type from the archive type identifier
     *
     * @param type archive type identifier defined by the archive implementation
     */
    internal fun getArchiveId(type: String): Int? {
        return archiveTypeIdentifiers[type]
    }

    /**
     * Create a [FileEntity] and insert it into the file tree
     */
    private fun insertFileEntityIntoTree(
        name: String, parent: FileEntity?, isDirectory: Boolean, type: String?, archiveType: String?, resource: Int
    ): FileEntity {
        val file = if (parent == null) {
            FileEntity(name, null, isDirectory, type, archiveType, resource).also {
                fileTrees.add(it)
            }
        } else {
            FileEntity(name, parent, isDirectory, type, archiveType, resource).also {
                parent.childEntities.add(it)
            }
        }

        // add to mapping for querying
        fileResourceMapping[file.resource] = file

        return file
    }

    /**
     * Delete all views with the associated file
     *
     * @param file a file in workspace
     */
    private fun deleteCachedViews(file: FileEntity) {
        val views = file.viewEntities.iterator()
        while (views.hasNext()) {
            val view = views.next()
            File(resourceDirectory, view.resource.toString()).delete()
            views.remove()
            eventBroker.dispatch(FileSystemEvent.ViewDeletedEvent(file, view))
        }
    }

    /**
     * Open a resource (file or file view) and return an input stream
     */
    private fun openResource(resource: Int): FileInputStream {
        return File(resourceDirectory, resource.toString()).inputStream()
    }

    /**
     * Save the index to disk
     */
    private fun saveIndex() {
        this.indexFile.writeText(gson.toJson(this.index))
    }

    fun listDirectory(fileEntity: FileEntity): List<FileEntity> {
        require(fileEntity.isDirectory) { "can only list files in directories" }

        return transaction { fileEntity.childEntities }
    }

    private class WorkspaceIndex {
        /**
         * Current resource counter
         */
        private var currentFileIndex: Int = 0

        /**
         * Get the next free index for a new file
         */
        fun getNextFileIndex(): Int {
            return synchronized(this) {
                ++currentFileIndex
            }
        }
    }
}