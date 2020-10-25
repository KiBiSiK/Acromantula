package net.cydhra.acromantula.workspace

import com.google.gson.GsonBuilder
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.events.AddedResourceEvent
import net.cydhra.acromantula.workspace.filesystem.events.DeletedResourceEvent
import net.cydhra.acromantula.workspace.filesystem.events.UpdatedResourceEvent
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileReader
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
     * Import a resource into the workspace. This will create the file entity and save the resource content into the
     * workspace. This method will read the contents of the specified url. It is assumed, that it is a trusted source.
     *
     * @param name the name this resource will get in the file tree
     * @param url the url where to load the resource from
     * @param parent an optional parent for the resource to integrate it into the file tree
     */
    fun importResource(name: String, url: URL, parent: FileEntity? = null): FileEntity {
        val content = url.openStream().use { it.readBytes() }
        return transaction {
            val newEntity = FileEntity.new {
                this.name = name
                this.parent = parent
            }

            this@WorkspaceFileSystem.addResource(newEntity, content)
            return@transaction newEntity
        }
    }

    /**
     * Add a resource to the workspace and associate it with the given file entity.
     *
     * @param file the internal representation of the new entity. Gets modified to store the location of data
     * @param content the resource's content in raw binary form
     */
    fun addResource(file: FileEntity, content: ByteArray) {
        val newFile = File(this.resourceDirectory, (++this.index.currentFileIndex).toString())
        newFile.writeBytes(content)

        this.databaseClient.transaction {
            file.resource = this@WorkspaceFileSystem.index.currentFileIndex
        }

        saveIndex()
        EventBroker.fireEvent(AddedResourceEvent(file))
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
        val id = this.databaseClient.transaction {
            require(file.resource != null) { "this file (\"${file.name}\") is not associated with a resource." }
            file.resource!!
        }

        val channel = File(resourceDirectory, id.toString()).inputStream().channel
        return channel.use { it.map(FileChannel.MapMode.READ_ONLY, 0L, channel.size()) }
    }

    /**
     * Update the content of a resource from workspace. Throws [IllegalArgumentException] if the resource has not been
     * added to the workspace, yet.
     *
     * @param file the resource to update
     * @param newContent the new resource content
     */
    fun updateResource(file: FileEntity, newContent: ByteBuffer) {
        val id = this.databaseClient.transaction {
            require(file.resource != null) { "this file (\"${file.name}\") is not associated with a resource." }
            file.resource!!
        }

        val channel = File(resourceDirectory, id.toString())
            .apply { delete() }
            .apply { createNewFile() }
            .outputStream()
            .channel

        while (newContent.remaining() > 0) {
            channel.write(newContent)
        }

        channel.close()

        EventBroker.fireEvent(UpdatedResourceEvent(file))
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

        // TODO do the transaction first and then delete the file. Rollback the transaction if the deletion fails.
        if (id != null) {
            File(resourceDirectory, id.toString()).delete()

            transaction {
                file.resource = null
            }
        }
        EventBroker.fireEvent(DeletedResourceEvent(file))
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
     * Save the index to disk
     */
    private fun saveIndex() {
        this.indexFile.writeText(gson.toJson(this.index))
    }

    private class WorkspaceIndex {
        /**
         * Current resource counter
         */
        var currentFileIndex: Int = 0
    }
}