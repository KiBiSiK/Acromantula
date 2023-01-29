package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.PushbackInputStream

/**
 * Implementations import different files into the workspace using different strategies. Using [handles] the
 * strategies decide which implementation imports wich file. If two strategies return true on that call, the first
 * one in the list handles the file, so it should be avoided that multiple importers exist for the same file.
 */
interface ImporterStrategy<S : ImporterState> {

    suspend fun handles(fileName: String, fileContent: PushbackInputStream): Boolean

    /**
     * Override to generate an instance of [S] which will be used to hold state during this import
     *
     * @param fileName name of the file that triggered the import job
     * @param fileContent a [PushbackInputStream] that allows the importer to read a portion of the imported file for
     * setup.
     *
     */
    fun initializeImport(fileName: String, fileContent: PushbackInputStream): S? {
        return null
    }

    /**
     * Import a file into the workspace.
     *
     * @param parent optional. A parent [FileEntity] for the imported file for workspace organization
     * @param fileName the name of the file in the workspace
     * @param fileContent a [PushbackInputStream] containing the file data
     * @param job current importer job. Can be used to import more files in this job
     * @param state state associated with this importer job
     *
     * @return the file database handle and the file content, if it can be provided. If this method never assembles
     * the file content fully in an array, `null` is returned
     */
    suspend fun import(
        parent: FileEntity?, fileName: String, fileContent: PushbackInputStream, job: ImporterJob, state: S?
    ): Pair<FileEntity, ByteArray?>
}