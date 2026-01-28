package io.github.xyzboom.comprun.server

import ch.epfl.scala.bsp4j.BuildClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import org.eclipse.lsp4j.jsonrpc.Launcher
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.xyzboom.comprun.server.CompRunBuilderServer.Companion.logProviders
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors

class CompRunServer : CliktCommand() {
    companion object {
        private val logger = KotlinLogging.logger {}

        @JvmStatic
        fun main(args: Array<String>) {
            CompRunServer().main(args)
        }
    }

    private val port by option("-p", "--port").default("8123")

    override fun run() {
        logger.info { "Starting Compiler Runner BSP server on port $port..." }
        logProviders()
        val serverChannel = ServerSocketChannel.open()
        serverChannel.bind(InetSocketAddress(port.toInt()))
        val es = Executors.newCachedThreadPool()

        try {
            while (!Thread.currentThread().isInterrupted) {
                val clientChannel = serverChannel.accept()
                es.submit {
                    handleClientConnection(clientChannel)
                }
            }
        } catch (e: Exception) {
            if (!Thread.currentThread().isInterrupted) {
                System.err.println("Server error: ${e.message}")
                e.printStackTrace()
            }
        } finally {
            serverChannel.close()
            es.shutdown()
            logger.info { "Server shutdown complete" }
        }
    }

    private fun handleClientConnection(clientChannel: SocketChannel) {
        val clientEs = Executors.newCachedThreadPool()
        try {
            clientChannel.configureBlocking(true)
            val socket = clientChannel.socket()

            val localServer = CompRunBuilderServer()
            val launcher = Launcher.Builder<BuildClient>()
                .setOutput(socket.getOutputStream())
                .setInput(socket.getInputStream())
                .setLocalService(localServer)
                .setRemoteInterface(BuildClient::class.java)
                .setExecutorService(clientEs)
                .create()

            localServer.client = launcher.remoteProxy

            logger.info { "Starting BSP communication with client ${socket.inetAddress.hostAddress}" }
            val future = launcher.startListening()
            future.get()
            logger.info { "Client ${socket.inetAddress.hostAddress} disconnected" }
        } catch (e: Exception) {
            if (!Thread.currentThread().isInterrupted) {
                logger.error(e) { "Error handling client connection: ${e.message}" }
            }
        } finally {
            clientChannel.close()
            clientEs.shutdown()
        }
    }
}