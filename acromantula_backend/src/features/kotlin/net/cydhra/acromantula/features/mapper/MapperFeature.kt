package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.events.AddedResourceEvent

object MapperFeature {

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

    /**
     * Generate mappings for a given file entity and its content
     */
    suspend fun generateMappings(file: FileEntity, content: ByteArray) {

    }

    internal suspend fun onFileAdded(event: AddedResourceEvent) {
        // schedule generation of mappings but forget the deferred result (which is Unit anyway)
        WorkspaceService.getWorkerPool().submit { generateMappings(event.file, event.content) }.start()
    }
}