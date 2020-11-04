package net.cydhra.acromantula.features.exporter

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.OutputStream

/**
 * Strategy to export a file in a certain way. This is mostly used for archive formats, as non-archive files should
 * always be exported with the generic exporter strategy, as those should exist in binary format in the workspace.
 */
interface ExporterStrategy {

    /**
     * Export the contents of [fileEntity] into the given [outputStream]
     */
    fun exportFile(fileEntity: FileEntity, outputStream: OutputStream)
}