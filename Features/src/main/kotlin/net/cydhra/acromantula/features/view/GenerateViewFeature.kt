package net.cydhra.acromantula.features.view

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import java.io.OutputStream

/**
 * This feature holds a set of [ViewGeneratorStrategy]s that can be used to generate human-readable interpretations
 * of binary data, like disassemblies or image data.
 */
object GenerateViewFeature {

    private val logger = LogManager.getLogger()

    private val registeredGenerators = mutableMapOf<String, ViewGeneratorStrategy>()

    /**
     * Generate a representation for [fileEntity] using the strategy identified by [viewType]. If no view can be
     * generated for the file, because the strategy does not handle this file, `null` is returned. This does also
     * happen, if an exception is thrown during generation.
     */
    fun generateView(fileEntity: FileEntity, viewType: String): FileViewEntity? {
        val representation = fileEntity.views.find { it.type == viewType }
        if (representation != null) {
            logger.debug("reusing existing representation from ${representation.created}")
            return representation
        }

        if (!(registeredGenerators[viewType]?.handles(fileEntity)
                ?: throw IllegalArgumentException("View generator \"$viewType\" does not exist"))
        )
            return null

        logger.info("creating representation for \"${fileEntity.name}\"")
        // TODO this is insanely bad design. I recon I did this to prevent mass-view generation to cancel when view
        //  generation of a single file fails. But instead the supervisor tasks must catch the error and resume all
        //  other tasks instead of failing all tasks because of one exception. This error is not supposed to be
        //  caught here.
        return try {
            registeredGenerators[viewType]!!.generateView(fileEntity)
        } catch (t: Throwable) {
            logger.error("error while generating representation for ${fileEntity.name}", t)
            null
        }
    }

    /**
     * Exports binary content of a file representation into [outputStream]. The output stream is not closed afterwards.
     */
    fun exportView(representation: FileViewEntity, outputStream: OutputStream) {
        outputStream.write(WorkspaceService.getRepresentationContent(representation).readBytes())
    }

    /**
     * Whether the given [viewType] supports reconstruction from representation data.
     *
     * @see ViewGeneratorStrategy.supportsReconstruction
     */
    fun canReconstructFromView(viewType: String): Boolean {
        return this.registeredGenerators[viewType]?.supportsReconstruction
            ?: throw IllegalArgumentException("view type \"$viewType\" is unknown")
    }

    /**
     * Reconstruct a file from representation data. This way the user can change data in human-readable
     * representations and change files this way, instead of open-heart operations on the raw binary file.
     * This does only work if the given [viewType] supports reconstruction, which can be checked using
     * [canReconstructFromView].
     *
     * @param fileEntity the file entity to change
     * @param viewType the view type that is being converted into the file
     * @param buffer the raw binary data of the view, that is being converted back
     *
     * @see ViewGeneratorStrategy.supportsReconstruction
     */
    fun reconstructFromView(fileEntity: FileEntity, viewType: String, buffer: ByteArray) {
        logger.info("reconstructing \"${fileEntity.name}\" from view of type \"$viewType\"")
        this.registeredGenerators[viewType]?.reconstructFromView(fileEntity, buffer)
            ?: throw IllegalArgumentException("view type \"$viewType\" is unknown")
    }

    /**
     * Get the file extension of a specific view type or `null`, if this type does not exist or does not have an
     * extension specified
     */
    fun getFileExtension(viewType: String): String? {
        return registeredGenerators[viewType]?.fileType?.fileExtension
    }

    /**
     * Get a list of all available view types and their generated file type identifiers.
     *
     * @see [net.cydhra.acromantula.workspace.filesystem.FileType]
     */
    fun getViewTypes(): List<Pair<String, String>> {
        return this.registeredGenerators.keys
            .map { type -> type to registeredGenerators[type]!!.fileType.typeHierarchy }
            .toList()
    }

    /**
     * Register a [ViewGeneratorStrategy] at this feature
     */
    fun registerViewGenerator(viewGeneratorStrategy: ViewGeneratorStrategy) {
        this.registeredGenerators[viewGeneratorStrategy.viewType] = viewGeneratorStrategy
    }
}