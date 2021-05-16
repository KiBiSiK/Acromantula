package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.PushbackInputStream

/**
 * Generates mappings for specific symbols and their references for a specific file type. May generate different
 * symbol types at once for efficiency.
 */
interface MappingFactory {

    /**
     * A name for this mapping factory type (so it can be identified in logs)
     */
    val name: String

    /**
     * Returns true, if this factory wants to handle the given file to generate mappings for it
     */
    fun handles(file: FileEntity, content: PushbackInputStream): Boolean

    /**
     * Generate mappings for the given file and content
     */
    suspend fun generateMappings(file: FileEntity, content: PushbackInputStream)
}