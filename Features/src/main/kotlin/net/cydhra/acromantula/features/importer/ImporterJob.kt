package net.cydhra.acromantula.features.importer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.cydhra.acromantula.features.archives.ArchiveFeature
import net.cydhra.acromantula.features.mapper.MapperFeature.mapFile
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import java.io.InputStream
import java.io.PushbackInputStream
import kotlin.coroutines.coroutineContext

/**
 * A user-triggered unit of work. This may just involve one file being imported, or several if the unit of work
 * involved an archive. All files of this job are imported before handed to other stages in the pipeline (like
 * mapping). Importers may hold state during import inside this class using the ImporterState interface.
 */
class ImporterJob internal constructor(
    private val registeredImporters: List<ImporterStrategy<*>>,
    private val genericFileImporterStrategy: GenericFileImporterStrategy
) {

    companion object {
        private val logger = LogManager.getLogger()
    }

    private lateinit var importerStates: Map<ImporterStrategy<*>, ImporterState>

    /**
     * Initialize the importer job with the parameters of the current import unit
     *
     * @param fileName name of the import job file
     * @param fileStream input stream of the job file
     */
    internal fun initialize(fileName: String, fileStream: PushbackInputStream) {
        this.importerStates = this.registeredImporters.mapNotNull { importer ->
            importer.initializeImport(fileName, fileStream)?.let {
                importer to it
            }
        }.toMap()
    }

    /**
     * Import a file into the workspace within the context of the current import job
     *
     * @param parent a parent entity in the file tree, that gets this file as a
     * @param fileName name for the file in the workspace
     * @param fileStream an [InputStream] for the file content
     */
    suspend fun importFile(parent: FileEntity?, fileName: String, fileStream: InputStream) {
        logger.trace("importing \"$fileName\"")

        if (!ArchiveFeature.canAddFile(parent)) {
            throw IllegalArgumentException(
                ArchiveFeature.getArchiveType(parent) + " archives do not support adding external files"
            )
        }

        val pushbackStream = if (fileStream is PushbackInputStream) fileStream else PushbackInputStream(fileStream, 512)

        val importer =
            registeredImporters.firstOrNull { it.handles(fileName, pushbackStream) } ?: genericFileImporterStrategy
        val (file, content) = importer.import(parent, fileName, pushbackStream, this, getImporterState(importer))
        logger.trace("finished importing \"$fileName\"")

        CoroutineScope(coroutineContext).launch {
            mapFile(file, content)
        }
    }

    /**
     * Retrieve the importer state object of the given strategy from the current job. Returns null, if the strategy
     * did not register state for this job.
     */
    @Suppress("UNCHECKED_CAST") // enforced by class contract
    private fun <S : ImporterState> getImporterState(importerStrategy: ImporterStrategy<S>): S? {
        return this.importerStates[importerStrategy] as? S?
    }
}