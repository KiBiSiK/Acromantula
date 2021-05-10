package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.database.DatabaseManager
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReferenceDelegate
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbolTypeDelegate
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.events.AddedResourceEvent
import org.apache.logging.log4j.LogManager
import kotlin.reflect.KClass

object MapperFeature {

    private val logger = LogManager.getLogger()

    /**
     * Registered factories for generating mappings
     */
    private val mappingFactories = mutableListOf<MappingFactory>()

    private val registeredSymbolTypes =
        mutableMapOf<KClass<out AcromantulaSymbol>, ContentMappingSymbolTypeDelegate>()
    private val registeredReferenceTypes =
        mutableMapOf<KClass<out AcromantulaReference>, ContentMappingReferenceDelegate>()

    /**
     * Register a factory to generate mappings
     */
    fun registerMappingFactory(factory: MappingFactory) {
        this.mappingFactories += factory
    }

    /**
     * Insert a symbol into database
     */
    fun insertSymbolIntoDatabase(symbol: AcromantulaSymbol) {
        val type = getSymbolTypeDelegate(symbol)

        symbol.symbol = DatabaseManager.insertSymbol(
            type,
            symbol.getFile(),
            symbol.getIdentifier(),
            symbol.getName(),
            symbol.getLocation()
        )
    }

    /**
     * Insert a reference into database. The referenced symbol must have been inserted yet, otherwise an exception is
     * thrown.
     */
    fun insertReferenceIntoDatabase(reference: AcromantulaReference) {
        val type = this.registeredReferenceTypes.getOrPut(reference::class) {
            DatabaseManager.registerContentMappingReferenceType(
                reference.type,
                getSymbolTypeDelegate(reference.getReferencedSymbol())
            )
        }

        reference.reference = DatabaseManager.insertReference(
            type, reference.getReferencedSymbol().symbol,
            reference.getOwner()?.symbol,
            reference.getFile(),
            reference.getLocation()
        )
    }

    /**
     * Get the symbol's type delegate or register it, if it has not been registered
     *
     * @param symbol a [AcromantulaSymbol] instance
     */
    private fun getSymbolTypeDelegate(symbol: AcromantulaSymbol): ContentMappingSymbolTypeDelegate {
        return this.registeredSymbolTypes.getOrPut(symbol::class) {
            DatabaseManager.registerContentMappingSymbolType(symbol.type)
        }
    }

    /**
     * Generate mappings for a given file entity and its content
     */
    private fun generateMappings(file: FileEntity, content: ByteArray) {
        this.mappingFactories
            .filter { it.handles(file, content) }
            .forEach {
                logger.debug("generating [${it.name}] mappings for ${file.name}...")

                // start the mapper and forget the deferred result
                WorkspaceService.getWorkerPool().submit { it.generateMappings(file, content) }.start()
            }
    }

    @Suppress("RedundantSuspendModifier")
    internal suspend fun onFileAdded(event: AddedResourceEvent) {
        generateMappings(event.file, event.content)
    }
}