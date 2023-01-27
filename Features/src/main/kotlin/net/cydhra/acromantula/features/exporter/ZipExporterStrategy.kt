package net.cydhra.acromantula.features.exporter

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipExporterStrategy : ExporterStrategy {
    override val name: String = "zip"

    override val defaultFileExtension: String = "zip"

    override val supportedArchiveTypes: Collection<String> = emptyList()

    override fun exportFile(fileEntity: FileEntity, outputStream: OutputStream) {
        val zipOutputStream = ZipOutputStream(outputStream)
        if (fileEntity.isDirectory) {
            addToZipFile(zipOutputStream, "", fileEntity.children)
        } else {
            addToZipFile(zipOutputStream, "", Collections.singletonList(fileEntity))
        }
        zipOutputStream.close()
    }

    fun addToZipFile(zipOutputStream: ZipOutputStream, prefix: String, files: List<FileEntity>) {
        for (file in files) {
            val fileName = if (prefix.isNotBlank()) prefix + file.name else file.name
            zipOutputStream.putNextEntry(ZipEntry(fileName))

            if (file.isDirectory) { // directories already end in path-separator
                zipOutputStream.closeEntry()
                addToZipFile(zipOutputStream, fileName, file.children)
            } else {
                val content = WorkspaceService.getFileContent(file)
                zipOutputStream.write(content.readBytes())
                zipOutputStream.closeEntry()
            }
        }
    }

}