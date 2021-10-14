package net.cydhra.acromantula.features.transformer

import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * A transformer for files in the workspace. Transformation takes one FileEntity as its starting point, but may
 * include any number of files related to the transformation of the starting file (as in: recursive children of the
 * given starting point, or referenced files that are affected by transformations of the starting file, etc)
 */
interface FileTransformer {

    /**
     * Unique name of this transformer
     */
    val name: String

    /**
     * Performs the transformation defined by the implementation upon the given file entity (and any entities deemed
     * suitable by the implementation as well)
     *
     * @param fileEntity transformation starting from here
     */
    suspend fun transform(fileEntity: FileEntity)
}