package net.cydhra.acromantula.workspace.files

import com.google.gson.GsonBuilder
import net.cydhra.acromantula.database.FileEntity
import java.io.File
import java.io.FileReader

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
     * Add a resource to the workspace. The location of the resource is stored in the given file entity, therefore a
     * transaction must be present.
     *
     * @param file the internal representation of the new entity. Gets modified to store the location of data
     * @param content the resource's content in raw binary form
     */
    fun addResource(file: FileEntity, content: ByteArray) {
        val newFile = File(this.resourceDirectory, this.index.currentFileIndex++.toString())
        newFile.writeBytes(content)

        // TODO write index into file entity

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