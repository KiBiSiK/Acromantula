package net.cydhra.acromantula.workspace.files

import com.google.gson.GsonBuilder
import net.cydhra.acromantula.database.DirectoryEntity
import net.cydhra.acromantula.database.FileEntity
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileReader
import java.net.URL

/**
 * A facade to all file system interaction of a workspace. No other part within the workspace should directly
 * interact with files of the workspace.
 */
class WorkspaceFileSystem(private val workspacePath: File) {

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
    fun importResource(name: String, url: URL, parent: DirectoryEntity? = null): FileEntity {
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
     * Add a resource to the workspace. The location of the resource is stored in the given file entity, therefore a
     * transaction must be present.
     *
     * @param file the internal representation of the new entity. Gets modified to store the location of data
     * @param content the resource's content in raw binary form
     */
    fun addResource(file: FileEntity, content: ByteArray) {
        val newFile = File(this.resourceDirectory, (++this.index.currentFileIndex).toString())
        newFile.writeBytes(content)

        transaction {
            file.resource = this@WorkspaceFileSystem.index.currentFileIndex
        }

        saveIndex()
    }

    /**
     * Read the content of a resource from workspace. Throws [IllegalStateException] if the resource has not been
     * added to the workspace, yet.
     *
     * @param file the resource to read
     *
     * @return the raw binary content of the given resource
     */
    fun readResource(file: FileEntity): ByteArray {
        TODO()
    }

    /**
     * Update the content of a resource from workspace. Throws [IllegalStateException] if the resource has not been
     * added to the workspace, yet.
     *
     * @param file the resource to update
     * @param newContent the new resource content
     */
    fun updateResource(file: FileEntity, newContent: ByteArray) {
        TODO()
    }

    /**
     * Delete a resource's content from workspace. Does nothing if the resource did not exist.
     *
     * @param file the resource to delete.
     */
    fun deleteResource(file: FileEntity) {
        TODO()
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