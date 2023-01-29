package net.cydhra.acromantula.features.importer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.PushbackInputStream
import java.net.URL
import java.util.*

/**
 * Imports files into the workspace. [ImporterStrategies][ImporterStrategy] can be registered to handle files
 * differently. The importer is invoked before the file is added to the workspace. The strategy must handle adding to
 * the workspace itself, because it may schedule heavy-duty work that does not need to block adding the file to the
 * workspace. The strategy may invoke the importer recursively to import contents of archives  properly.
 *
 */
object ImporterFeature {

    private val logger = LogManager.getLogger()

    /**
     * Different strategies for importing files into workspace. Order is important, as the first strategy that claims
     * to handle a file has precedence over later strategies
     */
    private val registeredImporters = LinkedList<ImporterStrategy<*>>()

    /**
     * Fallback importer strategy that simply copies the file into workspace
     */
    private val genericFileImporterStrategy = GenericFileImporterStrategy()

    init {
        registerImporterStrategy(ArchiveImporterStrategy())
    }

    /**
     * Import a file into the workspace and call subsequent handlers on it (like mapper). This method is intended to
     * trigger an import job invoked by the user. This method must not be called by other importers. Use [importFile]
     * from other importers during an import job.
     *
     * @param parent a parent entity in the file tree, that gets this file as a
     * @param file URL pointing to the file
     */
    suspend fun startImportJob(parent: FileEntity?, file: URL) {
        val fileName = File(file.toURI()).name

        val fileStream = try {
            withContext(Dispatchers.IO) {
                file.openConnection().getInputStream()
            }
        } catch (e: Exception) {
            logger.error("Error while importing File['$fileName']:", e)
            return
        }

        val importerJob = ImporterJob(registeredImporters, genericFileImporterStrategy)
        val pushbackStream = PushbackInputStream(fileStream, 512)

        importerJob.initialize(fileName, pushbackStream)

        pushbackStream.use {
            importerJob.importFile(parent, fileName, pushbackStream)
        }
    }

    /**
     * Register a new strategy for importing files
     *
     * @param strategy a [ImporterStrategy] implementation
     * @param priority if true, the strategy is not appended to the list, but added on top of it. Strategies further
     * to the front of the list take precedence over strategies further back. So this can be used to override a
     * strategy, that is already in the list.
     */
    fun registerImporterStrategy(strategy: ImporterStrategy<*>, priority: Boolean = false) {
        if (priority) registeredImporters.addFirst(strategy)
        else registeredImporters.add(strategy)
    }
}