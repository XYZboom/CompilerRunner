package io.github.xyzboom.comprun.client

import ch.epfl.scala.bsp4j.Bsp4j
import ch.epfl.scala.bsp4j.BuildClientCapabilities
import ch.epfl.scala.bsp4j.BuildServer
import ch.epfl.scala.bsp4j.CompileParams
import ch.epfl.scala.bsp4j.InitializeBuildParams
import com.google.gson.JsonElement
import io.github.xyzboom.comprun.CompRunInitData
import io.github.xyzboom.comprun.ICompilerResult
import io.github.xyzboom.comprun.gson
import org.eclipse.lsp4j.jsonrpc.Launcher
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class BspCompilerClient(
    language: String,
    supplier: String,
    version: String,
    host: String = "127.0.0.1",
    port: Int = 8123,
) : AutoCloseable {
    private val socket = Socket(host, port)
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val launcher = Launcher.Builder<BuildServer>()
        .setOutput(socket.getOutputStream())
        .setInput(socket.getInputStream())
        .setLocalService(CompRunBuildClient())
        .setExecutorService(executor)
        .setRemoteInterface(BuildServer::class.java)
        .create()
    private val server = launcher.remoteProxy
    private val listening: Future<Void> = launcher.startListening()
    private val compilerArguments = listOf("-l", language, "-s", supplier, "-v", version, "--")

    init {
        server.buildInitialize(
            InitializeBuildParams(
                CompRunBuildClient.CLIENT_NAME,
                CompRunBuildClient.CLIENT_VERSION,
                Bsp4j.PROTOCOL_VERSION,
                "",
                BuildClientCapabilities(listOf(language)),
            ).apply {
                data = CompRunInitData(
                    versionMap = mapOf(language to mapOf(supplier to setOf(version))),
                )
            },
        ).get()
        server.onBuildInitialized()
    }

    fun compile(args: List<String>): ICompilerResult {
        val result = server.buildTargetCompile(
            CompileParams(emptyList()).apply {
                arguments = compilerArguments + args
            },
        ).get()
        val data = result.data
        return when (data) {
            is ICompilerResult.DataCompilerResult -> data
            is JsonElement -> gson.fromJson(data, ICompilerResult.DataCompilerResult::class.java)
            else -> ICompilerResult.CommonFailure(-1, "No compile result from CompilerRunner BSP server")
        }
    }

    override fun close() {
        try {
            server.buildShutdown().get()
            server.onBuildExit()
        } finally {
            socket.close()
            listening.cancel(true)
            executor.shutdownNow()
        }
    }
}
