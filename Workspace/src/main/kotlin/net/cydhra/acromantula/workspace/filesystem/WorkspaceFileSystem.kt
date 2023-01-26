package net.cydhra.acromantula.workspace.filesystem

import com.google.gson.GsonBuilder
import net.cydhra.acromantula.workspace.database.DatabaseClient
import net.cydhra.acromantula.workspace.disassembly.FileView
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.WritableByteChannel
import java.time.Instant
import java.util.*

/**
 * A facade to all file system interaction of a workspace. No other part within the workspace should directly
 * interact with files of the workspace.
 */
// TODO somehow handle exclusive write access to resources, so no two clients ever write the same resource at once
internal class WorkspaceFileSystem(private val workspacePath: File, private val databaseClient: DatabaseClient) {

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
     * Event broker for file system events
     */
    private val eventBroker = FileSystemEventBroker()

    /**
     * Listener for [eventBroker] that will sync all file system events into the backing database
     */
    private val fileSystemDatabaseSync = FileSystemDatabaseSync()

    /**
     * All file trees in the workspace. This list guarantees that only one file entity exists per file, and no two
     * concurrent file entities for the same file exist and get out of sync.
     */
    private val fileTrees = mutableListOf<FileEntity>()

    init {
        if (!resourceDirectory.exists())
            resourceDirectory.mkdirs()

        if (!indexFile.exists()) {
            index = WorkspaceIndex()
            saveIndex()
        } else {
            index = gson.fromJson(FileReader(indexFile), WorkspaceIndex::class.java)
        }

        eventBroker.registerObserver(fileSystemDatabaseSync)
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
        val fileEntity = if (parent == null) {
            FileEntity(name, Optional.empty(), false, "", Optional.empty(), resourceIndex).also {
                fileTrees.add(it)
            }
        } else {
            FileEntity(name, Optional.of(parent), false, "", Optional.empty(), resourceIndex).also {
                parent.childEntities.add(it)
            }
        }

        // dispatch file creation event which will sync the file back to the database
        eventBroker.dispatch(FileSystemEvent.FileCreatedEvent(fileEntity))

        // save the file index
        saveIndex()
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

        val channel = File(resourceDirectory, file.resource.toString())
            .apply { delete() }
            .apply { createNewFile() }
            .outputStream()
            .channel

        val contentBuffer = ByteBuffer.wrap(newContent)

        while (contentBuffer.remaining() > 0) {
            channel.write(contentBuffer)
        }

        channel.close()

        // delete cached representations as they are now invalid.
        deleteCachedViews(file)

        TODO("fire file update event")
    }

    /**
     * Rename a file or directory. This does not move the file, it only changes its own name.
     *
     * @param fileEntity file to rename
     * @param newName new file name without file path
     */
    fun renameResource(fileEntity: FileEntity, newName: String) {
        fileEntity.name = newName
        TODO("fire file rename event")
    }

    /**
     * Delete a file's content from workspace.
     *
     * @param file the file to delete.
     *
     * @throws IllegalArgumentException if the file is a directory
     */
    fun deleteFile(file: FileEntity) {
        require(!file.isDirectory) { "cannot open file stream of directory" }

        val backingResource = File(resourceDirectory, file.resource.toString())

        check(backingResource.exists()) { "file does not have a backing resource. Has it already been deleted?" }

        // remove from parent's children and unset parent, remove from file trees
        if (file.parent.isPresent) {
            file.parent.get().childEntities.remove(file)
            file.parent = Optional.empty()
        } else {
            fileTrees.remove(file)
        }

        // delete backing resource
        backingResource.delete()

        // delete cached representations as they are now invalid
        deleteCachedViews(file)

        TODO("fire delete file event")
    }

    /**
     * Move a file to a new location in the file tree.
     *
     * @param targetDirectory target directory or null if target is workspace root
     */
    fun moveResource(file: FileEntity, targetDirectory: FileEntity?) {
        if (file.parent.isPresent) {
            file.parent.get().childEntities.remove(file)
            file.parent = Optional.ofNullable(targetDirectory)
            targetDirectory?.childEntities?.add(file)
        } else {
            fileTrees.remove(file)
            file.parent = Optional.ofNullable(targetDirectory)
            targetDirectory?.childEntities?.add(file)
        }

        if (targetDirectory == null) {
            fileTrees.add(file)
        }

        TODO("fire file move event")
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
        TODO("not implemented")
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
     * @param type view generator name
     * @param content view resource content
     */
    fun createFileRepresentation(file: FileEntity, type: String, content: ByteArray): FileView {
        val resourceIndex = index.getNextFileIndex()
        val viewEntity = FileView(file, type, resourceIndex, Instant.now())

        val newFile = File(this.resourceDirectory, resourceIndex.toString()).apply { createNewFile() }
        newFile.writeBytes(content)

        TODO("fire file view creation event")

        return viewEntity
    }

    /**
     * Open a file view content in an [InputStream]
     *
     * @param view a file view entity to read
     */
    fun openFileRepresentation(view: FileView): InputStream {
        return openResource(view.resource)
    }

    /**
     * Get the file size of a representation resource without reading the resource from disk
     */
    fun getFileRepresentationSize(representation: FileView): Long {
        return File(resourceDirectory, representation.resource.toString()).length()
    }

    /**
     * Register an archive type by its identifier. The identifier is expected to be unique. If it is already present
     * in the database, the existing id will be reused. This means registering an archive twice will not fail
     */
    fun registerArchiveType(fileTypeIdentifier: String) {
        transaction {
            archiveTypeIdentifiers.put(
                fileTypeIdentifier,
                ArchiveTable.insertIgnoreAndGetId {
                    it[typeIdent] = fileTypeIdentifier
                }!!.value
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

        transaction {
            directory.archiveEntity = EntityID(archiveTypeIdentifiers[type]!!, ArchiveTable)
        }
    }

    /**
     * Delete all views with the associated file
     *
     * @param file a file in workspace
     */
    private fun deleteCachedViews(file: FileEntity) {
        val views = file.viewEntities.iterator()
        while (views.hasNext()) {
            File(resourceDirectory, views.next().resource.toString()).delete()
            views.remove()
            TODO("fire events to delete file views from database")
        }
    }

    /**
     * Open a resource (file or file view) and return an input stream
     */
    private fun openResource(resource: Int): InputStream {
        return File(resourceDirectory, resource.toString()).inputStream()
    }

    /**
     * Save the index to disk
     */
    private fun saveIndex() {
        this.indexFile.writeText(gson.toJson(this.index))
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