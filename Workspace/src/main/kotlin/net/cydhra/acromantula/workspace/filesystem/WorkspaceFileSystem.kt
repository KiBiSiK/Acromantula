package net.cydhra.acromantula.workspace.filesystem

import com.google.gson.GsonBuilder
import net.cydhra.acromantula.workspace.database.DatabaseClient
import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.disassembly.FileRepresentationTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.WritableByteChannel

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

    private val index: WorkspaceIndex
    private val gson = GsonBuilder().create()

    /**
     * Registered archive types
     */
    private val archiveTypeIdentifiers = mutableMapOf<String, Int>()

    private val eventBroker = FileSystemEventBroker()

    init {
        if (!resourceDirectory.exists())
            resourceDirectory.mkdirs()

        if (!indexFile.exists()) {
            index = WorkspaceIndex()
            saveIndex()
        } else {
            index = gson.fromJson(FileReader(indexFile), WorkspaceIndex::class.java)
        }

    }

    /**
     * Add a resource to the workspace and associate it with the given file entity.
     *
     * @param name name of the new file
     * @param parent database entity of the parent file. optional
     * @param content the resource's content in raw binary form
     */
    fun addResource(name: String, parent: FileEntity?, content: ByteArray): FileEntity {
        val newFile = File(this.resourceDirectory, (++this.index.currentFileIndex).toString())
        newFile.writeBytes(content)

        val file = this.databaseClient.transaction {
            FileEntity.new {
                this.name = name
                this.parent = parent
                this.resource = this@WorkspaceFileSystem.index.currentFileIndex
            }
        }

        saveIndex()
        return file
    }

    /**
     * Read the content of a resource from workspace. Throws [IllegalArgumentException] if the resource has not been
     * added to the workspace, yet.
     *
     * @param file the resource to read
     *
     * @return the raw binary content of the given resource as a direct buffer.
     */
    fun readResource(file: FileEntity): ByteBuffer {
        val channel = this.openResource(file).channel
        return channel.use { it.map(FileChannel.MapMode.READ_ONLY, 0L, channel.size()) }
    }

    /**
     * Offer the content of a file as an [InputStream]. Throws [IllegalArgumentException] if the resource has not been
     * added to the workspace, yet.
     *
     * @param file the resource to read
     *
     * @return an [InputStream] for the file
     */
    fun openResource(file: FileEntity): FileInputStream {
        val id = this.databaseClient.transaction {
            file.refresh()
            require(file.resource != null) { "this file (\"${file.name}\") is not associated with a resource." }
            file.resource!!
        }

        return File(resourceDirectory, id.toString()).inputStream()
    }

    /**
     * Update the content of a resource from workspace. Throws [IllegalArgumentException] if the resource has not been
     * added to the workspace, yet.
     *
     * @param file the resource to update
     * @param newContent the new resource content
     */
    fun updateResource(file: FileEntity, newContent: ByteArray) {
        val id = this.databaseClient.transaction {
            require(file.resource != null) { "this file (\"${file.name}\") is not associated with a resource." }
            file.resource!!
        }

        val channel = File(resourceDirectory, id.toString())
            .apply { delete() }
            .apply { createNewFile() }
            .outputStream()
            .channel

        val contentBuffer = ByteBuffer.wrap(newContent)

        while (contentBuffer.remaining() > 0) {
            channel.write(contentBuffer)
        }

        channel.close()

        // delete cached representations as they are now invalid
        transaction {
            FileRepresentation.find { FileRepresentationTable.file eq file.id.value }.forEach { fileRepresentation ->
                File(resourceDirectory, fileRepresentation.resource.toString()).delete()
            }

            FileRepresentationTable.deleteWhere { FileRepresentationTable.file eq file.id.value }
        }
    }

    fun renameResource(fileEntity: FileEntity, newName: String) {
        this.databaseClient.transaction {
            fileEntity.name = newName
        }
    }

    /**
     * Delete a resource's content from workspace. Does nothing if the resource did not exist.
     *
     * @param file the resource to delete.
     */
    fun deleteResource(file: FileEntity) {
        val id = this.databaseClient.transaction {
            file.resource
        }

        if (id != null) {
            transaction {
                File(resourceDirectory, id.toString()).delete()

                // set resource of file entity to null, the file entity is not deleted
                file.resource = null

                // delete cached representations as they are now invalid
                FileRepresentation.find { FileRepresentationTable.file eq file.id.value }.forEach { repr ->
                    File(resourceDirectory, repr.resource.toString()).delete()
                }

                // delete representations from database
                FileRepresentationTable.deleteWhere { FileRepresentationTable.file eq file.id.value }
            }

        }
    }

    /**
     * Move a file to a new location in the file tree.
     *
     * @param targetDirectory target directory or null if target is workspace root
     */
    fun moveResource(file: FileEntity, targetDirectory: FileEntity?) {
        transaction {
            file.parent = targetDirectory
        }
    }

    /**
     * Export a resource from the workspace by copying it into a channel. The channel is not closed afterwards. The
     * resource is read from disk.
     *
     * @throws IllegalArgumentException if the file is not added to the workspace yet
     */
    fun exportResource(file: FileEntity, output: WritableByteChannel) {
        val buffer = readResource(file)
        while (buffer.remaining() > 0) {
            output.write(buffer)
        }
    }

    /**
     * Get the file size of a resource without reading the resource from disk
     */
    fun getResourceSize(file: FileEntity): Long {
        val id = this.databaseClient.transaction {
            file.refresh()
            require(file.resource != null) { "this file (\"${file.name}\") is not associated with a resource." }
            file.resource!!
        }

        return File(resourceDirectory, id.toString()).length()
    }

    /**
     * Creates a file representation resource in workspace.
     */
    fun createFileRepresentation(file: FileEntity, type: String, content: ByteArray): FileRepresentation {
        val newFile = File(this.resourceDirectory, (++this.index.currentFileIndex).toString())
        newFile.writeBytes(content)

        return this.databaseClient.transaction {
            FileRepresentation.new {
                this.file = file
                this.type = type
                this.resource = index.currentFileIndex
                this.created = DateTime.now()
            }
        }
    }

    /**
     * Open a file representation in an [InputStream]
     */
    fun openFileRepresentation(representation: FileRepresentation): InputStream {
        val id = this.databaseClient.transaction {
            representation.refresh()
            representation.resource
        }

        return File(resourceDirectory, id.toString()).inputStream()
    }

    /**
     * Get the file size of a representation resource without reading the resource from disk
     */
    fun getFileRepresentationSize(representation: FileRepresentation): Long {
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
     */
    fun markAsArchive(directory: FileEntity, type: String) {
        transaction {
            directory.archiveEntity = EntityID(archiveTypeIdentifiers[type]!!, ArchiveTable)
        }
    }

    /**
     * Save the index to disk
     */
    private fun saveIndex() {
        this.indexFile.writeText(gson.toJson(this.index))
    }

    /**
     * Return the URL of a file within this file system.
     */
    fun getFileUrl(fileEntity: Int): URL {
        return File(this.resourceDirectory, fileEntity.toString()).toURI().toURL()
    }

    private class WorkspaceIndex {
        /**
         * Current resource counter
         */
        var currentFileIndex: Int = 0
    }
}