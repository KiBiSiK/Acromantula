package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * Generates mappings for specific symbols and their references for a specific file type. May generate different
 * symbol types at once for efficiency.
 */
interface MappingFactory {

    /**
     * Returns true, if this factory wants to handle the given file to generate mappings for it
     */
    fun handles(file: FileEntity, content: ByteArray): Boolean

    /**
     * Generate mappings for the given file and content
     */
    fun generateMappings(file: FileEntity, content: ByteArray)
}