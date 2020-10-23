package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.filesystem.DirectoryEntity
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.InputStream
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
    private val registeredImporters = LinkedList<ImporterStrategy>()

    /**
     * Fallback importer strategy that simply copies the file into workspace
     */
    private val genericFileImporterStrategy = GenericFileImporterStrategy()

    init {
        registerImporterStrategy(ArchiveImporterStrategy())
        registerImporterStrategy(ClassFileImporterStrategy())
    }

    /**
     * Import a file into the workspace
     *
     * @param parent a parent entity in the file tree, that gets this file as a
     * @param file URL pointing to the file
     */
    fun importFile(parent: DirectoryEntity?, file: URL) {
        val fileName = File(file.toURI()).name

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
    fun importFile(parent: DirectoryEntity?, fileName: String, fileStream: InputStream) {
        logger.trace("importing \"$fileName\"")
        val pushbackStream = if (fileStream is PushbackInputStream) fileStream else PushbackInputStream(fileStream)

        val importer =
            registeredImporters.firstOrNull { it.handles(fileName, pushbackStream) } ?: genericFileImporterStrategy
        importer.import(parent, fileName, pushbackStream)
    }

    /**
     * Register a new strategy for importing files
     *
     * @param strategy a [ImporterStrategy] implementation
     * @param priority if true, the strategy is not appended to the list, but added on top of it. Strategies further
     * to the front of the list take precedence over strategies further back. So this can be used to override a
     * strategy, that is already in the list.
     */
    fun registerImporterStrategy(strategy: ImporterStrategy, priority: Boolean = false) {
        if (priority)
            registeredImporters.addFirst(strategy)
        else
            registeredImporters.add(strategy)
    }
}