package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager

object MapperFeature {

    private val logger = LogManager.getLogger()

    private val registeredMappers = mutableListOf<FileMapper<*>>()

    fun registerMapper(mapper: FileMapper<*>) {
        registeredMappers += mapper
    }

    /**
     * Start a new mapper job. The mapper job is not being initialized.
     */
    fun startMapperJob(): MapperJob {
        return MapperJob(registeredMappers)
    }

    /**
     * Get all symbols in a file that match a given predicate
     *
     * @param file queried file
     * @param predicate optional predicate to filter the symbols
     */
    suspend fun getSymbolsInFile(
        file: FileEntity, predicate: ((AcromantulaSymbol) -> Boolean)? = null
    ): Collection<AcromantulaSymbol> {
        // todo we could fork-join here, but retrieving should be so fast that the overhead isn't worth it? maybe
        //  check against a large workspace
        // fork-join would utilize
        // ```coroutineScope { registeredMappers.map {
        //        async {
        //          it.getSymbolsInFile(file, predicate)
        //        }
        //      }.awaitAll().flatten()
        //    }
        return registeredMappers.flatMap { it.getSymbolsInFile(file, predicate) }
    }

    /**
     * Get all references in a file that match a given predicate
     *
     * @param file queried file
     * @param predicate optional predicate to filter the references
     */
    suspend fun getReferencesInFile(
        file: FileEntity, predicate: ((AcromantulaReference) -> Boolean)?
    ): Collection<AcromantulaReference> {
        // todo see fork-join todo above
        return registeredMappers.flatMap { it.getReferencesInFile(file, predicate) }
    }

    /**
     * Get all references to a given symbol. This includes references that have been added by mappers that are not
     * the origin of the symbol
     *
     * @param symbol an acromantula symbol
     */
    suspend fun getReferencesToSymbol(symbol: AcromantulaSymbol): Collection<AcromantulaReference> {
        // todo see fork-join todo above
        return registeredMappers.flatMap { it.getReferencesToSymbol(symbol) }
    }
}