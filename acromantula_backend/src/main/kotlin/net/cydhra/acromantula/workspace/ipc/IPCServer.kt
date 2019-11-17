package net.cydhra.acromantula.workspace.ipc

import org.apache.commons.lang3.SystemUtils
import org.apache.logging.log4j.LogManager
import org.scalasbt.ipcsocket.UnixDomainServerSocket
import org.scalasbt.ipcsocket.Win32NamedPipeServerSocket
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * Serves a named pipe / unix socket for communication with potential front ends
 */
class IPCServer {

    private val logger = LogManager.getLogger()

    private lateinit var server: ServerSocket

    /**
     * A list of currently connected clients
     */
    private val connectedClients = CopyOnWriteArrayList<ClientThread>()

    /**
     * The IPC server does not use coroutines thus threads are managed in its own threadpool
     */
    private lateinit var cachedThreadPool: ExecutorService

    private var clientCounter: Int = 0

    /**
     * Setup the socket for IPC. It will be created and a server will wait for communication attempts. The server
     * will wait in a new thread, so this method won't block after the IPC channel has been established.
     */
    fun hostEndpoint() {
        cachedThreadPool = Executors.newCachedThreadPool()

        when {
            SystemUtils.IS_OS_WINDOWS -> {
                val pipeName = """\\.\pipe\api"""

                logger.info("creating named pipe at $pipeName")
                server = Win32NamedPipeServerSocket(pipeName)
            }
            SystemUtils.IS_OS_UNIX -> {
                val socketPath = File("api.sock")

                logger.info("creating unix socket at $socketPath")
                server = UnixDomainServerSocket(socketPath.toString())
            }
            else -> {
                throw IllegalStateException("this operating system is unsupported as no IPC can be provided")
            }
        }

        cachedThreadPool.submit(this::serverLoop)
    }

    fun shutdown() {
        logger.info("shutting down IPC clients...")
        this.connectedClients.forEach(ClientThread::shutdown)

        logger.info("closing IPC server...")
        server.close()

        logger.info("shutting down IPC thread pool...")
        this.cachedThreadPool.shutdown()
        if (!this.cachedThreadPool.awaitTermination(4, TimeUnit.SECONDS)) {
            logger.warn("IPC thread pool did not shut down cooperatively. Preemption...")
            this.cachedThreadPool.shutdownNow()
            if (this.cachedThreadPool.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                logger.info("Preemption successful.")
            } else {
                logger.warn("Preemption failed. Thread pool leaked!")
            }
        }

        logger.info("IPC server shutdown complete")
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
            connectedClients += clientThread
        }
    }

    /**
     * A class modelling a connected IPC client.
     */
    inner class ClientThread(private val clientSocket: Socket, private val id: Int) : Runnable {

        private val logger = LogManager.getLogger("IPC.${id}")

        private val output = DataOutputStream(clientSocket.getOutputStream())
        private val input = DataInputStream(clientSocket.getInputStream())

        override fun run() {
            try {
                do {
                    val line = readPacket(input)

                    logger.info("server recv: $line")
                    // TODO handle
                } while (line.trim { it <= ' ' } != "bye")
            } catch (e: IOException) {
                logger.debug("exception from IPC client:", e)

                output.close()
                input.close()
                clientSocket.close()
            }

            logger.info("IPC client [$id] disconnected")
            this@IPCServer.connectedClients -= this
        }

        /**
         * Read a packet of data by first reading a signed big-endian integer stating the size of the payload and
         * then reading bytes given by this size and interpreting them as an UTF-8 String.
         */
        private fun readPacket(inputStream: DataInputStream): String {
            val length = inputStream.readShort()
            require(length > 0) { "client sent packet with negative size. Terminating..." }

            val buf = ByteArray(length.toInt())
            inputStream.read(buf)

            return String(buf, Charset.forName("UTF-8"))
        }

        /**
         * Write a string to the stream by encoding its length as a signed big-endian integer and then
         * writing it as UTF-8 encoded binary data.
         */
        private fun writePacket(data: String, output: DataOutputStream) {
            val bytes = data.toByteArray()
            output.writeInt(bytes.size)
            output.write(bytes)
        }

        fun shutdown() {
            logger.debug("shutdown IPC client...")

            writePacket("bye", output)
            output.close()
            input.close()
            clientSocket.close()

            logger.debug("IPC resources closed")
        }
    }
}