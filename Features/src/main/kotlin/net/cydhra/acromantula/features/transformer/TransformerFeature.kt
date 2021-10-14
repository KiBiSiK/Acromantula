package net.cydhra.acromantula.features.transformer

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager.getLogger as logger


object TransformerFeature {

    private val registeredTransformers = mutableMapOf<String, FileTransformer>()

    /**
     * Register a new implementation of [FileTransformer] at this feature. Its name must be unique.
     */
    fun registerTransformer(transformer: FileTransformer) {
        if (registeredTransformers.containsKey(transformer.name))
            throw IllegalArgumentException("a transformer with name \"${transformer.name}\" is already registered")

        logger().debug("registering transformer \"${transformer.name}\"")
        this.registeredTransformers[transformer.name] = transformer
    }

    /**
     * Get the names of registered transformers as a list
     */
    fun getTransformers(): List<String> {
        return this.registeredTransformers.keys.toList()
    }

    /**
     * Transform files beginning with a given file. The transformer might choose additional files at will according to
     * its implementation.
     *
     * @param start file to start with
     * @param transformerName name of the transformer to use
     *
     * @throws IllegalArgumentException if [transformerName] is not registered
     */
    suspend fun transformFiles(start: FileEntity, transformerName: String) {
        val transformer = this.registeredTransformers[transformerName]
            ?: throw IllegalArgumentException("a transformer with name \"$transformerName\" does not exist")

        logger().info("begin transformation with \"${start.name}\" with transformer $transformer")
        transformer.transform(start)
    }
}