package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager

/**
 * A unit of work for the mapping feature. Each job is triggered by the user, but may involve many files (e.g. if an
 * archive is imported).
 */
class MapperJob(private val registeredMappers: List<FileMapper<*>>) {
    companion object {
        private val logger = LogManager.getLogger()
    }

    private lateinit var mapperStates: Map<FileMapper<*>, MapperState>

    /**
     * Initialize the mapper job with the parameters of the current job
     *
     * @param file the file name that triggered the mapper job
     */
    internal fun initialize(file: String) {
        logger.info("setup mapping job for ${file}...")

        this.mapperStates = this.registeredMappers.mapNotNull { mapper ->
            mapper.initializeMapper(file)?.let {
                mapper to it
            }
        }.toMap()
    }

    /**
     * Generate mappings for a new file.
     * @param file database file entity
     * @param content file binary content or null if the file is a directory
     */
    suspend fun mapFile(file: FileEntity, content: ByteArray?) {
        logger.trace("mapping file ${file.name}")
        registeredMappers.forEach {
            @Suppress("UNCHECKED_CAST") // enforced through class contract
            (it as FileMapper<in MapperState>).mapFile(file, content, getMapperState(it))
            logger.trace("finished mapping file ${file.name}")
        }
    }

    /**
     * Retrieve the [MapperState] object of the given mapper from the current job. Returns null, if the strategy
     * did not register state for this job.
     */
    @Suppress("UNCHECKED_CAST") // enforced by class contract
    private fun <S : MapperState> getMapperState(fileMapper: FileMapper<S>): S? {
        return this.mapperStates[fileMapper] as? S?
    }
}