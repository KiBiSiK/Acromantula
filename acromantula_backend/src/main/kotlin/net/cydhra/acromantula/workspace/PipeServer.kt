package net.cydhra.acromantula.workspace

import org.apache.commons.lang3.SystemUtils
import org.apache.logging.log4j.LogManager
import org.scalasbt.ipcsocket.UnixDomainServerSocket
import org.scalasbt.ipcsocket.Win32NamedPipeServerSocket
import java.net.ServerSocket
import java.nio.file.Files


/**
 * Serves a named pipe / unix socket for communication with potential front ends
 */
class PipeServer {

    private val logger = LogManager.getLogger()

    private lateinit var server: ServerSocket

    /**
     * Setup the socket for IPC. It will be created and a server will wait for communication attempts.
     */
    fun hostEndpoint() {
        when {
            SystemUtils.IS_OS_WINDOWS -> {
                val pipeName = """\\.\pipe\api"""

                logger.info("creating named pipe at $pipeName")
                server = Win32NamedPipeServerSocket(pipeName)
            }
            SystemUtils.IS_OS_UNIX -> {
                val tempDir = Files.createTempDirectory("ipcsocket")
                val sock = tempDir.resolve("api.sock")
                server = UnixDomainServerSocket(sock.toString())
            }
            else -> {
                throw IllegalStateException("this operating system is unsupported as no IPC can be provided")
            }
        }
    }
}