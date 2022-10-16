package net.cydhra.acromantula.features.exporter

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.OutputStream

/**
 * Strategy to export a file in a certain way. This is mostly used for archive formats, as non-archive files should
 * always be exported with the generic exporter strategy, as those should exist in binary format in the workspace.
 */
interface ExporterStrategy {

    val name: String

    /**
     * The default extension this exporter uses. This will not be appended to a file name, but can be used by the
     * front-end to auto-complete user actions. The extension is given without path delimiters or wildcards. Example:
     * "zip"
     */
    val defaultFileExtension: String
        get() = ""

    /**
     * Supported archive types this strategy can export. No restrictions if empty.
     */
    val supportedArchiveTypes: Collection<String>

    /**
     * Export the contents of [fileEntity] into the given [outputStream]
     */
    fun exportFile(fileEntity: FileEntity, outputStream: OutputStream)
}