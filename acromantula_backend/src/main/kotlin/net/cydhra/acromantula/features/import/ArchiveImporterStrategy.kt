package net.cydhra.acromantula.features.import

import net.cydhra.acromantula.data.DirectoryEntity
import net.cydhra.acromantula.features.import.util.ArchiveTreeBuilder
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal class ArchiveImporterStrategy : ImporterStrategy {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override fun handles(fileName: String, fileContent: ByteArray): Boolean {
        return try {
            val zipIn = ZipInputStream(ByteArrayInputStream(fileContent))
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun import(parent: DirectoryEntity, fileName: String, fileContent: ByteArray) {
        val zipInputStream = ZipInputStream(ByteArrayInputStream(fileContent))
        val treeBuilder = ArchiveTreeBuilder(fileName)

        var currentEntry: ZipEntry? = zipInputStream.nextEntry
        while (currentEntry != null) {
            logger.trace("importing ${currentEntry.name}")

            // do not test for `sDirectory` explicitly here, as java accepts zip files whose folders contain file
            // content. Just check whether content is available and treat it as a file, if there is.
            if (zipInputStream.available() > 0) {
                val entryContent: ByteArray = zipInputStream.readBytes()

                treeBuilder.addFileEntry(currentEntry.name, entryContent)
            }

            currentEntry = zipInputStream.nextEntry
        }

//        return@transaction treeBuilder.create()
        TODO()
    }

}