package net.cydhra.acromantula.workspace

import org.h2.jdbcx.JdbcDataSource
import java.io.File
import javax.sql.DataSource

class WorkspaceServer(private val workspaceFile: File) {

    private lateinit var dataSource: DataSource

    fun initialize() {
        dataSource = JdbcDataSource()
            .also {
                it.setURL("jdbc:h2:${workspaceFile.toURI().toURL()};DB_CLOSE_DELAY=10;MVCC=true")
            }
    }
}