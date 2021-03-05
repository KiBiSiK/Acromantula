package net.cydhra.acromantula.workspace.database

import net.cydhra.acromantula.workspace.database.mapping.*

/**
 * Manager for mappings within the workspace. [ContentMappingReferenceType]s and [ContentMappingSymbolType]s must be
 * registered here and will then be registered within the database
 */
object DatabaseManager {

    /**
     * The current [DatabaseClient]. When the database is closed and another one is opened, this reference must be
     * updated and all active mapping reference and symbol types must be reregistered at the new database.
     */
    private lateinit var databaseClient: DatabaseClient

    private val registeredContentMappingSymbolTypes = mutableListOf<ContentMappingSymbolTypeDelegate>()
    private val registeredContentMappingReferenceTypes = mutableListOf<ContentMappingReferenceDelegate>()

    /**
     * Must be called when a new database is loaded. This is NOT done automatically through event notification. This
     * will update all content mapping types, so they can be used with the new database.
     */
    internal fun setActiveDatabase(databaseClient: DatabaseClient) {
        this.databaseClient = databaseClient

        // update the mapping registrations (if upstream api clients have stored references to the delegates, nothing
        // breaks because the actual database entities are switched out transparently):
        registeredContentMappingSymbolTypes.forEach {
            it.symbolType = getOrInsertSymbolType(it.uniqueIdentifier)
        }

        registeredContentMappingReferenceTypes.forEach {
            it.referenceType = getOrInsertReferenceType(it.uniqueIdentifier, it.symbolType)
        }
    }

    /**
     * Register a [ContentMappingSymbolTypeDelegate] at the database that can be used to insert or select content mappings
     * of this special type. Symbol types must be registered, so the database can differentiate between symbol types
     * even if plugins are installed or uninstalled between sessions
     *
     * @param uniqueIdentifier a string that uniquely describes the symbol type among all types
     *
     * @return a delegate for the registered symbol type
     */
    fun registerContentMappingSymbolType(uniqueIdentifier: String): ContentMappingSymbolTypeDelegate {
        val mappingSymbolTypeEntity = getOrInsertSymbolType(uniqueIdentifier)
        val mappingSymbolTypeDelegate = ContentMappingSymbolTypeDelegate(uniqueIdentifier, mappingSymbolTypeEntity)
        registeredContentMappingSymbolTypes.add(mappingSymbolTypeDelegate)
        return mappingSymbolTypeDelegate
    }

    /**
     * Retrieve or insert a new symbol type in the database and return the database entity
     *
     * @param uniqueIdentifier reference type identifier
     */
    private fun getOrInsertSymbolType(uniqueIdentifier: String): ContentMappingSymbolType {
        return databaseClient.transaction {
            val preRegisteredType = ContentMappingSymbolType
                .find { ContentMappingSymbolTypeTable.identifier eq uniqueIdentifier }

            if (preRegisteredType.empty()) {
                ContentMappingSymbolType.new {
                    this.uniqueIdentifier = uniqueIdentifier
                }
            } else {
                preRegisteredType.first()
            }
        }
    }

    /**
     * Register a [ContentMappingReferenceType] at the database that can be used to insert or select content mappings
     * of this special type. Reference types must be registered, so the database can differentiate between reference
     * types even if plugins are installed or uninstalled between sessions
     *
     * @param uniqueIdentifier a string that uniquely describes the reference type among all reference types of the
     * given symbol type
     * @param symbol the mapping symbol references of this type refer to
     *
     * @return a delegate for the registered reference type
     */
    fun registerContentMappingReferenceType(
        uniqueIdentifier: String,
        symbol: ContentMappingSymbolTypeDelegate
    ): ContentMappingReferenceDelegate {
        val mappingReferenceTypeEntity = getOrInsertReferenceType(uniqueIdentifier, symbol)
        val mappingReferenceTypeDelegate =
            ContentMappingReferenceDelegate(uniqueIdentifier, symbol, mappingReferenceTypeEntity)
        registeredContentMappingReferenceTypes.add(mappingReferenceTypeDelegate)
        return mappingReferenceTypeDelegate
    }

    /**
     * Retrieve or insert a new reference type in the database and return the database entity
     *
     * @param uniqueIdentifier reference type identifier
     * @param symbol referenced symbol type
     */
    private fun getOrInsertReferenceType(
        uniqueIdentifier: String,
        symbol: ContentMappingSymbolTypeDelegate
    ): ContentMappingReferenceType {
        return databaseClient.transaction {
            val preRegisteredType = ContentMappingReferenceType
                .find { ContentMappingReferenceTypeTable.identifier eq uniqueIdentifier }

            if (preRegisteredType.empty()) {
                ContentMappingReferenceType.new {
                    this.uniqueIdentifier = uniqueIdentifier
                    this.symbolType = symbol.symbolType
                }
            } else {
                preRegisteredType.first()
            }
        }
    }
}