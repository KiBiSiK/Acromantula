package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.workspace.files.WorkspaceFileSystem
import org.h2.jdbcx.JdbcDataSource
import java.io.File
import javax.sql.DataSource

class WorkspaceServer(private val workspaceFile: File) {

    private lateinit var dataSource: DataSource

    /**
     * The access to binary resources that are stored directly in the file system
     */
    private val workspaceFileSystem = WorkspaceFileSystem(workspaceFile)

    fun initialize() {
        dataSource = JdbcDataSource()
            .also {
                it.setURL("jdbc:h2:${workspaceFile.toURI().toURL()};DB_CLOSE_DELAY=10;MVCC=true")
            }
    }
}