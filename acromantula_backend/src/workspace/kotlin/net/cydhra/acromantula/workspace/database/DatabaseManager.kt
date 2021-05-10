package net.cydhra.acromantula.workspace.database

import net.cydhra.acromantula.workspace.database.mapping.*
import net.cydhra.acromantula.workspace.filesystem.FileEntity

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

    private val contentMappingSymbolDelegates =
        mutableMapOf<ContentMappingSymbolType, ContentMappingSymbolTypeDelegate>()
    private val contentMappingReferenceDelegates =
        mutableMapOf<ContentMappingReferenceType, ContentMappingReferenceDelegate>()

    /**
     * Must be called when a new database is loaded. This is NOT done automatically through event notification. This
     * will update all content mapping types, so they can be used with the new database.
     */
    internal fun setActiveDatabase(databaseClient: DatabaseClient) {
        this.databaseClient = databaseClient

        contentMappingSymbolDelegates.clear()
        contentMappingReferenceDelegates.clear()

        // update the mapping registrations (if upstream api clients have stored references to the delegates, nothing
        // breaks because the actual database entities are switched out transparently):
        registeredContentMappingSymbolTypes.forEach {
            it.symbolType = getOrInsertSymbolType(it.uniqueIdentifier)
            contentMappingSymbolDelegates[it.symbolType] = it
        }

        registeredContentMappingReferenceTypes.forEach {
            it.referenceType = getOrInsertReferenceType(it.uniqueIdentifier, it.symbolType)
            contentMappingReferenceDelegates[it.referenceType] = it
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
        contentMappingSymbolDelegates[mappingSymbolTypeEntity] = mappingSymbolTypeDelegate
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
        contentMappingReferenceDelegates[mappingReferenceTypeEntity] = mappingReferenceTypeDelegate
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

    /**
     * Obtain the symbol delegate instance of the given symbol type
     */
    internal fun getSymbolDelegate(type: ContentMappingSymbolType): ContentMappingSymbolTypeDelegate {
        return this.contentMappingSymbolDelegates[type]!!
    }

    /**
     * Obtain the reference delegate instance of the given reference type
     */
    internal fun getReferenceTypeDelegate(type: ContentMappingReferenceType): ContentMappingReferenceDelegate {
        return this.contentMappingReferenceDelegates[type]!!
    }

    /**
     * Insert a symbol into the database.
     *
     * @param type type of symbol to insert
     * @param identifier preferably unique identifier for the symbol - this should (among all symbols of a given
     * type) uniquely describe wich symbol is meant. This is not enforced however.
     * @param name symbol name (may be part of the identifier, or the same). This is the part that is actually
     * changed by the (re-) mapper
     * @param location an unstructured string that hints where to find this symbol within the file
     *
     * @see ContentMappingSymbol
     */
    fun insertSymbol(
        type: ContentMappingSymbolTypeDelegate,
        file: FileEntity,
        identifier: String,
        name: String,
        location: String
    ): ContentMappingSymbol {
        return this.databaseClient.transaction {
            ContentMappingSymbol.new {
                this.type = type
                this.file = file
                this.identifier = identifier
                this.name = name
                this.location = location
            }
        }
    }

    /**
     * Insert a reference into the database
     *
     * @param type the reference type
     * @param symbol the symbol to reference
     * @param owner optional. A symbol that most closely "owns" this reference (to make locating the reference easier)
     * @param file the file this reference is in
     * @param location an unstructured string that hints where to find this reference within the file (or from the
     * owner)
     *
     * @see ContentMappingReference
     */
    fun insertReference(
        type: ContentMappingReferenceDelegate,
        symbol: ContentMappingSymbol,
        owner: ContentMappingSymbol?,
        file: FileEntity,
        location: String
    ): ContentMappingReference {
        return this.databaseClient.transaction {
            ContentMappingReference.new {
                this.type = type
                this.symbol = symbol
                this.owner = owner
                this.file = file
                this.location = location
            }
        }
    }
}