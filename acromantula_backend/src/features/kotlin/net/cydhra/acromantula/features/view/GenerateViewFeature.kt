package net.cydhra.acromantula.features.view

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager

/**
 * This feature holds a set of [ViewGeneratorStrategy]s that can be used to generate human-readable interpretations
 * of binary data, like disassemblies or image data.
 */
object GenerateViewFeature {

    private val logger = LogManager.getLogger()

    private val registeredGenerators = mutableMapOf<String, ViewGeneratorStrategy>()

    fun generateView(fileEntity: FileEntity, viewType: String) {
        // TODO
    }

    /**
     * Register a [ViewGeneratorStrategy] at this feature
     */
    fun registerViewGenerator(viewGeneratorStrategy: ViewGeneratorStrategy) {
        this.registeredGenerators[viewGeneratorStrategy.viewType] = viewGeneratorStrategy
    }
}