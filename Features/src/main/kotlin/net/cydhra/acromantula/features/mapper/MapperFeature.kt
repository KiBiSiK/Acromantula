package net.cydhra.acromantula.features.mapper

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.database.DatabaseMappingsManager
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReferenceDelegate
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbolTypeDelegate
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayInputStream
import java.io.PushbackInputStream

object MapperFeature {

    private val logger = LogManager.getLogger()

    /**
     * Registered factories for generating mappings
     */
    private val mappingFactories = mutableListOf<MappingFactory>()

    private val registeredSymbolTypes =
        mutableMapOf<AcromantulaSymbolType, ContentMappingSymbolTypeDelegate>()

    private val registeredReferenceTypes =
        mutableMapOf<AcromantulaReferenceType, ContentMappingReferenceDelegate>()

    /**
     * Register a factory to generate mappings
     */
    fun registerMappingFactory(factory: MappingFactory) {
        logger.trace("register mapping factory: ${factory.name}")
        this.mappingFactories += factory
    }

    /**
     * Register a symbol type at the database
     */
    fun registerSymbolType(symbolType: AcromantulaSymbolType) {
        logger.trace("register symbol type: ${symbolType.symbolType}")
        val typeDelegate = DatabaseMappingsManager.registerContentMappingSymbolType(symbolType.symbolType)
        this.registeredSymbolTypes[symbolType] = typeDelegate
    }

    /**
     * Register a symbol reference type at the database. The referenced symbol type must have already been registered.
     *
     * @param referenceType the reference type implementation
     * @param symbolType the symbol type referenced by this reference type
     */
    fun registerReferenceType(referenceType: AcromantulaReferenceType, symbolType: AcromantulaSymbolType) {
        logger.trace("register reference type: ${referenceType.referenceType}")
        if (!this.registeredSymbolTypes.containsKey(symbolType))
            throw IllegalStateException("the symbol type of this reference has not been registered yet")

        if (this.registeredReferenceTypes.containsKey(referenceType))
            throw IllegalStateException("this reference type has been registered before")

        val delegate = DatabaseMappingsManager.registerContentMappingReferenceType(
            referenceType.referenceType,
            this.registeredSymbolTypes[symbolType]!!
        )
        this.registeredReferenceTypes[referenceType] = delegate
    }

    /**
     * Insert a new symbol instance into the database.
     *
     * @param symbolType symbol type implementation
     * @param file the symbol's origin file, if known
     * @param symbolIdentifier a globally unique identifier for the symbol. The format of this parameter is chosen by
     * the implementation.
     * @param symbolName the local name of the symbol. The format of this parameter is chosen by the implementation.
     * @param location the location of the symbol within the file. The format of this parameter is chosen by the
     * implementation
     */
    suspend fun insertSymbolIntoDatabase(
        symbolType: AcromantulaSymbolType,
        file: FileEntity?,
        symbolIdentifier: String,
        symbolName: String,
        location: String?
    ) {
        logger.trace("insert \"${symbolType.symbolType}\" ($symbolIdentifier) at ($location) into database.")
        DatabaseMappingsManager.insertSymbol(
            this.registeredSymbolTypes[symbolType]!!,
            file?.id,
            symbolIdentifier,
            symbolName,
            location
        )
    }

    fun insertReferenceIntoDatabase(
        referenceType: AcromantulaReferenceType,
        file: FileEntity,
        symbolIdentifier: String,
        ownerIdentifier: String?,
        location: String?
    ) {
        if (!this.registeredReferenceTypes.containsKey(referenceType))
            throw IllegalStateException("this reference type has not been registered yet")

        logger.trace(
            "insert \"${referenceType.referenceType}\" for (${symbolIdentifier}) at ($location) into " +
                    "database."
        )
        DatabaseMappingsManager.insertReference(
            this.registeredReferenceTypes[referenceType]!!,
            symbolIdentifier,
            ownerIdentifier,
            file.id,
            location
        )
    }

    fun getReferences(type: String, symbol: String): List<Pair<Int, String>> {
        val typeDelegate = this.registeredSymbolTypes.entries
            .filter { (symbolType, _) -> symbolType.symbolType == type }
            .map { (_, delegate) -> delegate }
            .firstOrNull() ?: throw IllegalArgumentException("invalid symbol type \"$type\"")

        return DatabaseMappingsManager.findReferences(typeDelegate, symbol)
            .map { ref ->
                transaction {
                    ref.id.value to translateReferenceType(ref.type).stringRepresentation(ref)
                }
            }
    }

    private fun translateReferenceType(referenceDelegate: ContentMappingReferenceDelegate): AcromantulaReferenceType {
        return this.registeredReferenceTypes.entries.find { (_, d) -> d == referenceDelegate }!!.key
    }

    /**
     * Generate mappings for a given file entity and its content
     */
    fun startMappingTasks(supervisor: CompletableJob, file: FileEntity, content: ByteArray?) {
        val inputStream = if (content == null) {
            PushbackInputStream(WorkspaceService.getFileContent(file), 512)
        } else {
            PushbackInputStream(ByteArrayInputStream(content), 512)
        }

        this.mappingFactories
            .filter { it.handles(file, inputStream) }
            .forEach {
                logger.trace("generating [${it.name}] mappings for ${file.name}...")

                // start the mapper and forget the deferred result
                CoroutineScope(supervisor).launch { it.generateMappings(file, inputStream) }
            }
    }
}