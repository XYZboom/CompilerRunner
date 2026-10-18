package io.github.xyzboom.comprun.client

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.github.xyzboom.comprun.ICompilerResult
import io.github.xyzboom.comprun.cli.CliCompiler
import io.github.xyzboom.comprun.cli.main
import kotlin.system.exitProcess

class CompRunClient : CliCompiler() {

    private val args by argument().multiple()
    private val host by option("--host").default("127.0.0.1")
    private val port by option("--port").int().default(8123)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CompRunClient().main(args)
        }
    }

    override fun run(): ICompilerResult {
        BspCompilerClient(language, supplier, version, host, port).use { client ->
            val compilerResult = client.compile(args)
            if (compilerResult.success) {
                compilerResult.message?.let(::println)
            } else {
                compilerResult.message?.let(System.err::println)
            }
            exitProcess(compilerResult.exitCode)
        }
    }
}