package io.github.xyzboom.comprun.server

import ch.epfl.scala.bsp4j.BuildClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import org.eclipse.lsp4j.jsonrpc.Launcher
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

class CompRunServer : CliktCommand() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CompRunServer().main(args)
        }
    }

    override fun run() {
        val serverChannel = ServerSocketChannel.open()
        serverChannel.bind(InetSocketAddress(8123))
        val clientChannel = serverChannel.accept()
        clientChannel.configureBlocking(true)
        val socket = clientChannel.socket()

        val localServer = CompRunBuilderServer()
        val launcher = Launcher.Builder<BuildClient>()
            .setOutput(socket.getOutputStream())
            .setInput(socket.getInputStream())
            .setLocalService(localServer)
            .setRemoteInterface(BuildClient::class.java)
            .create()
        localServer.client = launcher.remoteProxy
        val future = launcher.startListening()
        println("Server started successfully")

        future.get()
    }
}