package net.cydhra.acromantula.workspace.files

import net.cydhra.acromantula.data.WorkspaceFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class WorkspaceFileSystemTest {

    /**
     * A temporary directory where to do the tests
     */
    private val wfsPath = File("wfstest")

    /**
     * The wfs instance to use in tests
     */
    private lateinit var wfs: WorkspaceFileSystem

    @BeforeEach
    fun setUp() {
        if (wfsPath.exists())
            wfsPath.delete()

        wfsPath.apply { mkdir() }
        wfs = WorkspaceFileSystem(wfsPath)
    }

    @AfterEach
    fun tearDown() {
        wfsPath.delete()
    }

    @Test
    fun importResource() {
    }

    @Test
    fun addResource() {
    }

    @Test
    fun readResource() {
    }

    @Test
    fun updateResource() {
    }

    @Test
    fun deleteResource() {
    }

    @Test
    fun exportResource() {
    }
}