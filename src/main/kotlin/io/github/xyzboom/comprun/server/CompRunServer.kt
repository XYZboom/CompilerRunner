package io.github.xyzboom.comprun.server

import ch.epfl.scala.bsp4j.BuildClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import org.eclipse.lsp4j.jsonrpc.Launcher
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors

class CompRunServer : CliktCommand() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CompRunServer().main(args)
        }
    }

    private val port by option("-p", "--port").default("8123")

    override fun run() {
        println("Starting Compiler Runner BSP server on port $port...")
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
            println("Server shutdown complete")
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

            println("Starting BSP communication with client ${socket.inetAddress.hostAddress}")
            val future = launcher.startListening()
            future.get()
            println("Client ${socket.inetAddress.hostAddress} disconnected")
        } catch (e: Exception) {
            if (!Thread.currentThread().isInterrupted) {
                System.err.println("Error handling client connection: ${e.message}")
                e.printStackTrace()
            }
        } finally {
            clientChannel.close()
            clientEs.shutdown()
        }
    }
}