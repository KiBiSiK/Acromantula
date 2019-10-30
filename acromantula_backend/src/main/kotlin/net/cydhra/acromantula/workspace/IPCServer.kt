package net.cydhra.acromantula.workspace

import org.apache.commons.lang3.SystemUtils
import org.apache.logging.log4j.LogManager
import org.scalasbt.ipcsocket.UnixDomainServerSocket
import org.scalasbt.ipcsocket.Win32NamedPipeServerSocket
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.util.concurrent.Executors


/**
 * Serves a named pipe / unix socket for communication with potential front ends
 */
class IPCServer {

    private val logger = LogManager.getLogger()

    private lateinit var server: ServerSocket

    /**
     * The IPC server does not use coroutines thus threads are managed in its own threadpool
     */
    private val cachedThreadPool = Executors.newCachedThreadPool()

    private var clientCounter: Int = 0

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
                val socketPath = tempDir.resolve("api.sock")

                logger.info("creating unix socket at $socketPath")
                server = UnixDomainServerSocket(socketPath.toString())
            }
            else -> {
                throw IllegalStateException("this operating system is unsupported as no IPC can be provided")
            }
        }

        cachedThreadPool.submit(this::serverLoop)
    }

    /**
     * Accept client "sockets" of the IPC server and handle their requests
     */
    private fun serverLoop() {
        logger.info("IPC socket listening...")

        while (true) {
            val clientSocket = server.accept()

            logger.info("IPC connection [${++clientCounter}] established...")
            val clientThread = ClientThread(clientSocket, clientCounter)
            cachedThreadPool.submit(clientThread)
        }
    }

    class ClientThread(private val clientSocket: Socket, private val id: Int) : Runnable {

        private val logger = LogManager.getLogger("IPC.${id}")

        private val output = PrintWriter(clientSocket.getOutputStream(), true)
        private val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

        override fun run() {
            try {
                var line: String?

                do {
                    line = input.readLine()
                    if (line != null) {
                        logger.trace("server recv: $line")

                        // TODO handle
                    }
                } while (line!!.trim { it <= ' ' } != "bye")
            } catch (e: IOException) {
                logger.error("error from IPC socket", e)
            }

            logger.info("IPC client [$id] disconnected")
        }
    }
}