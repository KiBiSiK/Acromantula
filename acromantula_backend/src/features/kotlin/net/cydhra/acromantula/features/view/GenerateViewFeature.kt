package net.cydhra.acromantula.features.view

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager

/**
 * This feature holds a set of [ViewGeneratorStrategy]s that can be used to generate human-readable interpretations
 * of binary data, like disassemblies or image data.
 */
object GenerateViewFeature {

    private val logger = LogManager.getLogger()

    private val registeredGenerators = mutableMapOf<String, ViewGeneratorStrategy>()

    /**
     * Generate a representation for [fileEntity] using the strategy identified by [viewType]. If no view can be
     * generated for the file, because the strategy does not handle this file, `null` is returned.
     */
    fun generateView(fileEntity: FileEntity, viewType: String): FileRepresentation? {
        val representation = WorkspaceService.queryRepresentation(fileEntity, viewType)
        if (representation != null) {
            return representation
        }

        if (!(registeredGenerators[viewType]?.handles(fileEntity)
                ?: throw IllegalArgumentException("View generator \"$viewType\" does not exist"))
        )
            return null

        return registeredGenerators[viewType]!!.generateView(fileEntity)
    }

    /**
     * Register a [ViewGeneratorStrategy] at this feature
     */
    fun registerViewGenerator(viewGeneratorStrategy: ViewGeneratorStrategy) {
        this.registeredGenerators[viewGeneratorStrategy.viewType] = viewGeneratorStrategy
    }
}