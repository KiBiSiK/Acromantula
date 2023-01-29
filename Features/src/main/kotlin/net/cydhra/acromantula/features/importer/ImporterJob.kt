package net.cydhra.acromantula.features.importer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.cydhra.acromantula.features.archives.ArchiveFeature
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.features.mapper.MapperJob
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

    /**
     * All importer states by registered importer strategies
     */
    private lateinit var importerStates: Map<ImporterStrategy<*>, ImporterState>

    /**
     * Mapper job this importer will use downstream
     */
    private lateinit var mapperJob: MapperJob

    /**
     * Initialize the importer job with the parameters of the current import unit
     *
     * @param fileName name of the import job file
     * @param fileStream input stream of the job file
     */
    internal fun initialize(fileName: String, fileStream: PushbackInputStream) {
        logger.info("setup importer job for ${fileName}...")

        this.importerStates = this.registeredImporters.mapNotNull { importer ->
            importer.initializeImport(fileName, fileStream)?.let {
                importer to it
            }
        }.toMap()
    }

    /**
     * Start the import job from [ImporterFeature]. Any subsequent import must call [importFile].
     *
     * @param parent user-defined parent directory.
     * @param fileName name of the import job file
     * @param fileStream input stream of the import job file
     */
    internal suspend fun startImportJob(parent: FileEntity?, fileName: String, fileStream: InputStream) {
        if (!ArchiveFeature.canAddFile(parent)) {
            throw IllegalArgumentException(
                ArchiveFeature.getArchiveType(parent) + " archives do not support adding external files"
            )
        }

        mapperJob = MapperFeature.startMapperJob()
        mapperJob.initialize(fileName)

        val (file, content) = invokeImporterStrategy(parent, fileName, fileStream)

        CoroutineScope(coroutineContext).launch {
            mapperJob.mapFile(file, content)
        }
    }

    /**
     * Import a file into the workspace within the context of the current import job
     *
     * @param parent a parent entity in the file tree
     * @param fileName name for the file in the workspace
     * @param fileStream an [InputStream] for the file content
     */
    suspend fun importFile(parent: FileEntity?, fileName: String, fileStream: InputStream) {
        val (file, content) = invokeImporterStrategy(parent, fileName, fileStream)

        CoroutineScope(coroutineContext).launch {
            mapperJob.mapFile(file, content)
        }
    }

    private suspend fun invokeImporterStrategy(
        parent: FileEntity?, fileName: String, fileStream: InputStream
    ): Pair<FileEntity, ByteArray?> {
        logger.trace("importing \"$fileName\"")
        val pushbackStream = if (fileStream is PushbackInputStream) fileStream else PushbackInputStream(fileStream, 512)

        @Suppress("UNCHECKED_CAST") val importer =
            (registeredImporters.firstOrNull { it.handles(fileName, pushbackStream) }
                ?: genericFileImporterStrategy) as ImporterStrategy<in ImporterState>
        val result = importer.import(parent, fileName, pushbackStream, this, getImporterState(importer))
        logger.trace("finished importing \"$fileName\"")

        return result
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