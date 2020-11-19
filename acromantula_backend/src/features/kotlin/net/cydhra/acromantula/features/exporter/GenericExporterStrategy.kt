package net.cydhra.acromantula.features.exporter

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.OutputStream

const val GENERIC_EXPORTER_STRATEGY = "generic"

class GenericExporterStrategy : ExporterStrategy {
    override val name: String = GENERIC_EXPORTER_STRATEGY

    override fun exportFile(fileEntity: FileEntity, outputStream: OutputStream) {
        WorkspaceService.exportFile(fileEntity, outputStream)
    }

}