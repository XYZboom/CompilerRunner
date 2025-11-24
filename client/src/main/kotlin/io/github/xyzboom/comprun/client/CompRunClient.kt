package io.github.xyzboom.comprun.client

import ch.epfl.scala.bsp4j.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.google.gson.JsonElement
import io.github.xyzboom.comprun.CompRunInitData
import io.github.xyzboom.comprun.ICompilerResult
import io.github.xyzboom.comprun.cli.CliCompiler
import io.github.xyzboom.comprun.cli.main
import io.github.xyzboom.comprun.gson
import org.eclipse.lsp4j.jsonrpc.Launcher
import java.net.Socket
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class CompRunClient : CliCompiler() {

    private val args by argument().multiple()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CompRunClient().main(args)
        }
    }

    override fun run(): ICompilerResult {
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
        launcher.startListening()
        server.buildInitialize(
            InitializeBuildParams(
                CompRunBuildClient.CLIENT_NAME,
                CompRunBuildClient.CLIENT_VERSION,
                Bsp4j.PROTOCOL_VERSION,
                "",
                BuildClientCapabilities(
                    listOf(language)
                )
            ).apply {
                data = CompRunInitData(
                    versionMap = mapOf(
                        language to mapOf(supplier to setOf(this@CompRunClient.version))
                    )
                )
            }
        ).get()
        server.onBuildInitialized()
        val compileResult = server.buildTargetCompile(CompileParams(emptyList()).apply {
            arguments = listOf("-l", language, "-s", supplier, "-v", version, "--") + args.toList()
        }).get()
        val data = compileResult.data
        if (data is JsonElement) {
            val compilerResult = gson.fromJson(data, ICompilerResult.DataCompilerResult::class.java)
            if (compilerResult.success) {
                println(compilerResult.message)
            } else {
                System.err.println(compilerResult.message)
            }
            exitProcess(compilerResult.exitCode)
        }
        System.err.println("WARN: No compile result from server")
        exitProcess(-1)
    }
}