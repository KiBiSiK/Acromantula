package net.cydhra.acromantula.features.mapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.cydhra.acromantula.workspace.filesystem.FileEntity

object MapperFeature {

    private val registeredMappers = mutableListOf<FileMapper>()

    fun registerMapper(mapper: FileMapper) {
        registeredMappers += mapper
    }

    /**
     * Generate mappings for a new file.
     * @param file database file entity
     * @param content file binary content
     */
    fun CoroutineScope.mapFile(file: FileEntity, content: ByteArray) {
        registeredMappers.forEach { launch { it.mapFile(file, content) } }
    }
}