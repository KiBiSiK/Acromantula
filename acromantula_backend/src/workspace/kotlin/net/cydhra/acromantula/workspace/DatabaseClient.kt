package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.disassembly.DisassemblyTable
import net.cydhra.acromantula.workspace.filesystem.*
import net.cydhra.acromantula.workspace.java.*
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.net.URL
import java.sql.ResultSet
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
        dataSource = JdbcDataSource()
            .also {
                it.setURL("jdbc:h2:$databasePath;DB_CLOSE_DELAY=10;MVCC=true")
            }

        database = Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                DirectoryTable,
                ArchiveTable,
                FileTable,
                IndexFileTable,
                IndexMetaDatumTable,
                DisassemblyTable,
                JavaIdentifierTable,
                JavaClassTable,
                JavaMethodTable,
                JavaFieldTable,
                JavaAnnotationTable,
                JavaModuleTable,
                JavaParameterTable,
                JavaSourceFileTable,
                MemberReferenceTable,
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
    fun directQuery(query: String): ResultSet {
        val statement = database.transactionManager.currentOrNull()!!.connection.createStatement()
        statement.execute(query)
        return statement.resultSet
    }
}