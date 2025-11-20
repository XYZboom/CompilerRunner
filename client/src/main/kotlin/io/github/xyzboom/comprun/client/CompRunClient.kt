package io.github.xyzboom.comprun.client

import ch.epfl.scala.bsp4j.Bsp4j
import ch.epfl.scala.bsp4j.BuildClientCapabilities
import ch.epfl.scala.bsp4j.BuildServer
import ch.epfl.scala.bsp4j.InitializeBuildParams
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import io.github.xyzboom.comprun.CompRunInitData
import org.eclipse.lsp4j.jsonrpc.Launcher
import java.net.Socket
import java.util.concurrent.Executors

class CompRunClient : CliktCommand() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CompRunClient().main(args)
        }
    }

    override fun run() {
        val socket = Socket("127.0.0.1", 8123)
        val localClient = CompRunBuildClient()
        val es = Executors.newFixedThreadPool(1)
        val launcher = Launcher.Builder<BuildServer>()
            .setOutput(socket.getOutputStream())
            .setInput(socket.getInputStream())
            .setLocalService(localClient)
            .setExecutorService(es)
            .setRemoteInterface(BuildServer::class.java)
            .create()
        val server = launcher.getRemoteProxy()
        val future = launcher.startListening()
        server.buildInitialize(
            InitializeBuildParams(
                CompRunBuildClient.CLIENT_NAME,
                CompRunBuildClient.CLIENT_VERSION,
                Bsp4j.PROTOCOL_VERSION,
                "/home/xyzboom/Code/kotlin/SSReducer/out/test1",
                BuildClientCapabilities(
                    listOf("kotlin")
                )
            ).apply {
                data = CompRunInitData(versionMap = mapOf("kotlin" to setOf("2.2.21")))
            }
        ).get()
        server.onBuildInitialized()
        future.get()
    }
}