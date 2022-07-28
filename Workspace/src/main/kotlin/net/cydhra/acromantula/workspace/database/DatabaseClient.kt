package net.cydhra.acromantula.workspace.database

import net.cydhra.acromantula.workspace.disassembly.FileRepresentationTable
import net.cydhra.acromantula.workspace.filesystem.ArchiveTable
import net.cydhra.acromantula.workspace.filesystem.FileTable
import net.cydhra.acromantula.workspace.filesystem.IndexMetaDatumTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.sqlite.SQLiteDataSource
import java.net.URL
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


    fun connect() {
        dataSource = SQLiteDataSource()
            .also {
                it.url = "jdbc:sqlite:$databasePath"
            }

        database = Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                ArchiveTable,
                FileTable,
                IndexMetaDatumTable,
                FileRepresentationTable,
            )
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
            database.transactionManager.currentOrNull()!!.exec(
                object : Statement<List<List<String>>>(StatementType.SELECT, emptyList()) {
                    override fun PreparedStatementApi.executeInternal(transaction: Transaction): List<List<String>> {
                        return executeQuery().use { resultSet ->
                            val columnCount = resultSet.metaData.columnCount
                            val resultList = mutableListOf<List<String>>()

                            (1..columnCount)
                                .map { resultSet.metaData.getColumnName(it) }
                                .toList()
                                .also(resultList::add)

                            while (resultSet.next()) {
                                (1..columnCount)
                                    .map { resultSet.getObject(it)?.toString() ?: "null" }
                                    .toList()
                                    .also(resultList::add)
                            }

                            resultList
                        }
                    }

                    override fun prepareSQL(transaction: Transaction): String = query

                    override fun arguments(): Iterable<Iterable<Pair<IColumnType, Any?>>> = listOf()
                })
        }!!
    }
}