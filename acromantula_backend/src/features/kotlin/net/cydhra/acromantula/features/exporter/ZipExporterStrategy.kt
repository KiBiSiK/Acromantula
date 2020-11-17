package net.cydhra.acromantula.features.exporter

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipExporterStrategy : ExporterStrategy {
    override val name: String = "zip"

    override fun exportFile(fileEntity: FileEntity, outputStream: OutputStream) {
        if (!fileEntity.isDirectory) {
            throw IllegalArgumentException("only directories can be exported as zip files")
        }

        val zipOutputStream = ZipOutputStream(outputStream)
        val subFiles = WorkspaceService.getDirectoryContent(fileEntity)
        addToZipFile(zipOutputStream, "", subFiles)
        zipOutputStream.close()
    }

    fun addToZipFile(zipOutputStream: ZipOutputStream, prefix: String, files: List<FileEntity>) {
        for (file in files) {
            val fileName = if (prefix.isNotBlank()) prefix + File.pathSeparatorChar + file.name else file.name

            if (file.isDirectory) {
                zipOutputStream.putNextEntry(ZipEntry("$fileName/"))
                zipOutputStream.closeEntry()
                val subFiles = WorkspaceService.getDirectoryContent(file)
                addToZipFile(zipOutputStream, fileName, subFiles)
            } else {
                val content = WorkspaceService.getFileContent(file)
                zipOutputStream.putNextEntry(ZipEntry(fileName))
                zipOutputStream.write(content.readBytes())
                zipOutputStream.closeEntry()
            }
        }
    }

}