package net.cydhra.acromantula.workspace.commands

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.cydhra.acromantula.workspace.WorkspaceService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Test the command dispatcher interface
 */
internal class CommandDispatcherTest {

    /**
     * An example command for the workspace that shall simply yield success.
     */
    private class WorkspaceEchoCommand : WorkspaceCommand {
        override val type: String = "test/command/echo"
    }

    @BeforeEach
    fun setUp() {
        runBlocking {
            WorkspaceService.initialize()
        }
    }

    @Test
    fun dispatchCommand() {
        runBlocking {
            WorkspaceService.commandDispatcher.dispatchCommand(WorkspaceEchoCommand())
            delay(10L)
        }

        // TODO: verify that a success event has been fired
    }
}