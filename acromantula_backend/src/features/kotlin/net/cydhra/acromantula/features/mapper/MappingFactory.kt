package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * Generates mappings for specific symbols and their references for a specific file type. May generate different
 * symbol types at once for efficiency.
 */
interface MappingFactory {

    fun generateMappings(file: FileEntity, content: ByteArray)
}