package net.cydhra.acromantula.features.exporter

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.OutputStream

class GenericExporterStrategy : ExporterStrategy {
    override fun exportFile(fileEntity: FileEntity, outputStream: OutputStream) {
        WorkspaceService.exportFile(fileEntity, outputStream)
    }

}