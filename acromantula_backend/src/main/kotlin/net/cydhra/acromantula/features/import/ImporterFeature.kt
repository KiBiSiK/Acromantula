package net.cydhra.acromantula.features.import

import net.cydhra.acromantula.data.filesystem.DirectoryEntity
import org.apache.logging.log4j.LogManager
import java.io.InputStream
import java.io.PushbackInputStream
import java.net.URL

/**
 * Imports files into the workspace. [ImporterStrategies][ImporterStrategy] can be registered to handle files
 * differently. The importer is invoked before the file is added to the workspace. The strategy must handle adding to
 * the workspace itself, because it may schedule heavy-duty work that does not need to block adding the file to the
 * workspace. The strategy may invoke the importer recursively to import contents of archives  properly.
 *
 */
object ImporterFeature {

    private val logger = LogManager.getLogger()

    private val registeredImporters = mutableListOf<ImporterStrategy>()

    /**
     * Import a file into the workspace
     *
     * @param parent a parent entity in the file tree, that gets this file as a
     * @param fileName name for the file in the workspace
     * @param file URL pointing to the file
     */
    fun importFile(parent: DirectoryEntity, fileName: String, file: URL) {
        val fileStream = try {
            file.openConnection().getInputStream()
        } catch (e: Exception) {
            logger.error("Error while importing File['$fileName']:", e)
            return
        }

        importFile(parent, fileName, fileStream)
    }

    /**
     * Import a file into the workspace
     *
     * @param parent a parent entity in the file tree, that gets this file as a
     * @param fileName name for the file in the workspace
     * @param fileStream an [InputStream] for the file content
     */
    fun importFile(parent: DirectoryEntity, fileName: String, fileStream: InputStream) {
        val pushbackStream = if (fileStream is PushbackInputStream) fileStream else PushbackInputStream(fileStream)

        val importer = registeredImporters.first { it.handles(fileName, pushbackStream) }
        importer.import(parent, fileName, pushbackStream)
    }

    /**
     * Register a new strategy for importing files
     */
    fun registerImporterStrategy(strategy: ImporterStrategy) {
        registeredImporters += strategy
    }
}