package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.events.AddedResourceEvent
import org.apache.logging.log4j.LogManager

object MapperFeature {

    private val logger = LogManager.getLogger()

    /**
     * Registered factories for generating mappings
     */
    private val mappingFactories = mutableListOf<MappingFactory>()

    /**
     * Register a factory to generate mappings
     */
    fun registerMappingFactory(factory: MappingFactory) {
        this.mappingFactories += factory
    }

    fun insertSymbolIntoDatabase(symbol: AcromantulaSymbol) {

    }

    fun insertReferenceIntoDatabase(reference: AcromantulaReference) {

    }

    /**
     * Generate mappings for a given file entity and its content
     */
    private fun generateMappings(file: FileEntity, content: ByteArray) {
        this.mappingFactories
            .filter { it.handles(file, content) }
            .forEach {
                logger.debug("generating [${it.name}] mappings for ${file.name}...")

                // start the mapper and forget the deferred result
                WorkspaceService.getWorkerPool().submit { it.generateMappings(file, content) }.start()
            }
    }

    @Suppress("RedundantSuspendModifier")
    internal suspend fun onFileAdded(event: AddedResourceEvent) {
        generateMappings(event.file, event.content)
    }
}