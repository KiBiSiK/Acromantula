package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * Implemented by plugins to generate mappings for any form of files.
 */
interface FileMapper {

    /**
     * Called by the import feature after importing a file into workspace
     *
     * @param file database file entity
     * @param content file binary content
     */
    suspend fun mapFile(file: FileEntity, content: ByteArray)

    /**
     * Retrieve all symbols in a file that are managed by this mapper implementation
     *
     * @param file file entity
     * @param predicate optional filter rule to select specific symbols
     */
    suspend fun getSymbolsInFile(
        file: FileEntity,
        predicate: ((AcromantulaSymbol) -> Boolean)? = null
    ): Collection<AcromantulaSymbol>

    /**
     * Retrieve all references in a file that are managed by this mapper implementation
     */
    suspend fun getReferencesInFile(
        file: FileEntity,
        predicate: ((AcromantulaReference) -> Boolean)?
    ): Collection<AcromantulaReference>

    /**
     * Retrieve all references to a symbol that are managed by this mapper. The symbol is not necessarily managed by
     * this mapper.
     *
     * @param symbol any symbol implementation by any plugin
     */
    suspend fun getReferencesToSymbol(symbol: AcromantulaSymbol): Collection<AcromantulaReference>
}