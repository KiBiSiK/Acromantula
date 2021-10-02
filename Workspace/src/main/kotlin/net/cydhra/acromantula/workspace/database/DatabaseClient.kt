package net.cydhra.acromantula.workspace.database

import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReferenceDelegate
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReferenceTable
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbolTable
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbolTypeDelegate
import net.cydhra.acromantula.workspace.disassembly.FileRepresentationTable
import net.cydhra.acromantula.workspace.filesystem.ArchiveTable
import net.cydhra.acromantula.workspace.filesystem.FileTable
import net.cydhra.acromantula.workspace.filesystem.IndexMetaDatumTable
import org.apache.logging.log4j.LogManager
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.net.URL
import java.util.*
import javax.sql.DataSource

/**
 * A database client connecting to the database identified by `databasePath`. This can either be a URL or the term
 * `mem:<name>`
 */
internal class DatabaseClient(private val databasePath: String) {

    /**
     * A client that connects to a database identified by a url. Can be file or network URL.
     */
    constructor(url: URL) : this(url.toString())

    private lateinit var dataSource: DataSource

    private lateinit var database: Database

    /**
     * A cache for symbol insertion, so while a lot of symbols are concurrently inserted into the database, they do not
     * have to be retrieved constantly to guarantee uniqueness of their entity within the application.
     *
     * The cache must be initialized at least once and should be flushed (and thereby destroyed) after a mass
     * insertion to free up the RAM
     */
    private lateinit var identifierCache: Hashtable<String, Symbol>

    /**
     * A cache for reference insertion, so while symbols aren't being inserted yet due to caching in
     * [identifierCache], references to those symbols can be held back, too.
     */
    private lateinit var referenceCache: HashSet<Reference>

    private var cacheInitialized: Boolean = false

    fun connect() {
        dataSource = JdbcDataSource()
            .also {
                it.setURL("jdbc:h2:$databasePath;DB_CLOSE_DELAY=10")
            }

        database = Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                ArchiveTable,
                FileTable,
                IndexMetaDatumTable,
                FileRepresentationTable,
                ContentMappingSymbolTable,
                ContentMappingReferenceTable
            )
        }
    }

    /**
     * Initialize the symbol cache with initial capacity, loadFactor and estimated number of accessing threads.
     */
    fun initializeSymbolCache(capacity: Int, loadFactor: Float) {
        synchronized(cacheInitialized) {
            if (!cacheInitialized) {
                identifierCache = Hashtable(capacity, loadFactor)
                referenceCache = HashSet(capacity * 10, loadFactor)
                cacheInitialized = true
            } else
                LogManager.getLogger().warn("skipped cache initialization because cache is in use")
        }
    }

    /**
     * Empty the symbol cache and reduce its size
     */
    fun flushSymbolCache() {
        synchronized(cacheInitialized) {
            transaction {
                ContentMappingSymbolTable.batchInsert(
                    data = identifierCache.values,
                    ignore = false,
                    shouldReturnGeneratedValues = false
                ) { symbol ->
                    this[ContentMappingSymbolTable.id] = symbol.identifier
                    this[ContentMappingSymbolTable.type] = symbol.type.symbolType.id
                    this[ContentMappingSymbolTable.file] = symbol.file
                    this[ContentMappingSymbolTable.name] = symbol.name
                    this[ContentMappingSymbolTable.location] = symbol.location
                }

                ContentMappingReferenceTable.batchInsert(
                    data = referenceCache,
                    ignore = false,
                    shouldReturnGeneratedValues = false
                ) { reference ->
                    this[ContentMappingReferenceTable.symbol] = reference.symbolIdentifier
//                    if (reference.ownerIdentifier != null)
//                        this[ContentMappingReferenceTable.owner] = reference.ownerIdentifier
                    this[ContentMappingReferenceTable.type] = reference.type.referenceType.id
                    this[ContentMappingReferenceTable.file] = reference.file
                    this[ContentMappingReferenceTable.location] = reference.location
                }
            }
            identifierCache = Hashtable()
            cacheInitialized = false
        }
    }

    /**
     * Transaction into the connected database
     */
    fun <T> transaction(statement: Transaction.() -> T): T {
        return transaction(database, statement)
    }

    /**
     * Execute a direct, unprepared query on the database. This is a debug method and is not meant to be used by
     * production code.
     */
    fun directQuery(query: String): List<List<String>> {
        return this.transaction {
            val statement = database.transactionManager.currentOrNull()!!.connection.prepareStatement(query, true)
            statement.executeQuery()
            val resultSet = statement.resultSet!!
            val columnCount = resultSet.metaData.columnCount

            val resultList = mutableListOf<List<String>>()

            (1..columnCount)
                .map { resultSet.metaData.getColumnName(it) }
                .toList()
                .also(resultList::add)

            while (resultSet.next()) {
                (1..columnCount)
                    .map { resultSet.getObject(it)?.toString() ?: "" }
                    .toList()
                    .also(resultList::add)
            }

            resultList
        }
    }

    fun insertSymbolIntoCache(
        type: ContentMappingSymbolTypeDelegate,
        file: EntityID<Int>?,
        identifier: String,
        name: String,
        location: String?
    ) {
        val newSymbol = Symbol(
            type,
            file,
            identifier,
            name,
            location
        )
        val hashCode = newSymbol.hashCode()
        if (this.identifierCache.containsKey(identifier)) {
            if (this.identifierCache[identifier]!!.file == null && newSymbol.file != null) {
                this.identifierCache[identifier] = newSymbol
            }
        } else {
            this.identifierCache[identifier] = newSymbol
        }

    }

    fun insertReferenceIntoCache(
        type: ContentMappingReferenceDelegate,
        symbolIdentifier: String,
        ownerIdentifier: String?,
        file: EntityID<Int>,
        location: String?
    ) {
        this.referenceCache.add(
            Reference(
                type = type,
                symbolIdentifier = symbolIdentifier,
                ownerIdentifier = ownerIdentifier,
                file = file,
                location = location
            )
        )
    }

    private class Symbol(
        val type: ContentMappingSymbolTypeDelegate,
        val file: EntityID<Int>?,
        val identifier: String,
        val name: String,
        val location: String?
    ) {
        override fun hashCode(): Int {
            return identifier.hashCode()
        }
    }

    private data class Reference(
        val type: ContentMappingReferenceDelegate,
        val symbolIdentifier: String,
        val ownerIdentifier: String?,
        val file: EntityID<Int>,
        val location: String?
    )
}