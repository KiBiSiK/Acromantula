package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.bus.events.ApplicationShutdownEvent
import net.cydhra.acromantula.workspace.filesystem.ArchiveEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import net.cydhra.acromantula.workspace.worker.WorkerPool
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.and
import java.io.File

/**
 * Facade service for the workspace sub-system. Everything related to data storage and data operation is delegated
 * from here.
 */
object WorkspaceService : Service {

    override val name: String = "workspace-service"

    private val logger = LogManager.getLogger()

    /**
     * The client of the current workspace connection.
     */
    private lateinit var workspaceClient: WorkspaceClient

    /**
     * Called upon application startup. Load default workspace and subscribe to events if necessary.
     */
    override suspend fun initialize() {
        workspaceClient = LocalWorkspaceClient(File(".tmp"))
        workspaceClient.initialize()

        EventBroker.registerEventListener(ApplicationShutdownEvent::class, ::onShutdown)
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun onShutdown(@Suppress("UNUSED_PARAMETER") e: ApplicationShutdownEvent) {
        this.workspaceClient.shutdown()
    }

    /**
     * Get the common thread pool for the current workspace
     */
    fun getWorkerPool(): WorkerPool {
        return workspaceClient.workerPool
    }

    /**
     * Add an archive entry into the workspace file tree. Since the archive is only parent to its content, no actual
     * data is associated with it. It is simply a [DirectoryEntity] that has an [ArchiveEntity] associated
     *
     * @param archiveName simple name of the archive
     */
    fun addArchiveEntry(archiveName: String, parent: FileEntity?): FileEntity {
        logger.trace("creating archive entry in file tree: \"$archiveName\"")
        return workspaceClient.databaseClient.transaction {
            val archive = ArchiveEntity.new {}

            FileEntity.new {
                this.name = archiveName
                this.parent = parent
                this.isDirectory = true
                this.archiveEntity = archive
            }
        }
    }

    /**
     * Add a directory entry into the workspace file tree.
     */
    fun addDirectoryEntry(name: String, parent: FileEntity?): FileEntity {
        logger.trace("creating directory entry in file tree: \"$name\"")
        return workspaceClient.databaseClient.transaction {
            FileEntity.new {
                this.name = name
                this.parent = parent
                this.isDirectory = true
            }
        }
    }

    /**
     * Add a file into the workspace. An entry in database is create for reference and the content is uploaded into
     * the workspace.
     *
     * @param name file name
     * @param parent optional parent directory
     * @param content file binary content
     */
    fun addFileEntry(name: String, parent: FileEntity?, content: ByteArray): FileEntity {
        logger.trace("creating file entry in file tree: \"$name\"")
        val fileEntity = workspaceClient.databaseClient.transaction {
            FileEntity.new {
                this.name = name
                this.parent = parent
            }
        }

        workspaceClient.uploadFile(fileEntity, content)
        return fileEntity
    }

    /**
     * Add a file into workspace and parse its contents as a java class and insert its members into database. The
     * file is uploaded into the workspace.
     *
     * @param name file name
     * @param parent optional parent directory
     * @param content file binary content (bytecode)
     */
    fun addClassEntry(name: String, parent: FileEntity?, content: ByteArray): FileEntity {
        val fileEntity = addFileEntry(name, parent, content)

        logger.trace("scheduling class parsing for: \"$name\"")
        @Suppress("DeferredResultUnused")
        this.getWorkerPool().submit {
            workspaceClient.classParser.import(
                content,
                this@WorkspaceService.workspaceClient.databaseClient,
                fileEntity
            )
        }

        return fileEntity
    }

    fun queryDirectory(path: String): FileEntity {
        val pathParts = path.split("/")
        return this.workspaceClient.databaseClient.transaction {
            var index = pathParts.size - 1

            // TODO this query should be replaced by a singular recursive query, but for now it at least works for
            //  unique directory names
            var results = FileEntity.find { FileTable.name eq pathParts[index--] }

            while (index >= 0) {
                when {
                    results.empty() -> error("directory with path $path does not exist")
                    results.count() == 1 -> return@transaction results.first()
                    else -> TODO("implement recursive query to search for parent directory")
                }
            }

            error("directory with path $path does not exist")
        }
    }

    fun queryDirectory(id: Int): FileEntity {
        return this.workspaceClient.databaseClient.transaction {
            FileEntity.find { FileTable.id eq id and (FileTable.isDirectory eq true) }.firstOrNull()
                ?: error("directory with id $id does not exist")
        }
    }

    /**
     * A debug function to directly execute a raw, unprepared SQL query on the workspace database. This function
     * should not be called in production builds, but is only meant for debugging the database from the CLI
     */
    fun directQuery(query: String): List<List<String>> {
        return this.workspaceClient.databaseClient.directQuery(query)
    }

}