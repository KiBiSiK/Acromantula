package net.cydhra.acromantula.features.importer

import kotlinx.coroutines.CompletableJob
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.PushbackInputStream

/**
 * Implementations import different files into the workspace using different strategies. Using [handles] the
 * strategies decide which implementation imports wich file. If two strategies return true on that call, the first
 * one in the list handles the file, so it should be avoided that multiple importers exist for the same file.
 */
interface ImporterStrategy {

    suspend fun handles(fileName: String, fileContent: PushbackInputStream): Boolean

    /**
     * Import a file into the workspace.
     *
     * @param supervisor the supervisor job that supervises the import and is used for subsequent imports (for archives)
     * @param parent optional. A parent [FileEntity] for the imported file for workspace organization
     * @param fileName the name of the file in the workspace
     * @param fileContent a [PushbackInputStream] containing the file data
     *
     * @return the file database handle and the file content, if it can be provided. If this method never assembles
     * the file content fully in an array, `null` is returned
     */
    suspend fun import(
        supervisor: CompletableJob,
        parent: FileEntity?,
        fileName: String,
        fileContent: PushbackInputStream
    ): Pair<FileEntity, ByteArray?>
}