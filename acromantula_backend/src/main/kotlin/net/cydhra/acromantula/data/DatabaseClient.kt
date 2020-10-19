package net.cydhra.acromantula.data

import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import javax.sql.DataSource

class DatabaseClient(private val url: URL) {

    private lateinit var dataSource: DataSource

    private lateinit var database: Database

    fun connect() {
        dataSource = JdbcDataSource()
            .also {
                it.setURL("jdbc:h2:$url;DB_CLOSE_DELAY=10;MVCC=true")
            }

        database = Database.connect(this.dataSource)
    }

    /**
     * Transaction into the connected database
     */
    fun <T> transaction(statement: Transaction.() -> T): T {
        return transaction(database, statement)
    }
}