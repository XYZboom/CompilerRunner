package io.github.xyzboom.comprun.cli

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.clikt.parsers.flatten
import io.github.xyzboom.comprun.ICompilerResult

abstract class CliCompiler : BaseCliktCommand<CliCompiler>() {
    abstract fun run(): ICompilerResult
}

fun CliCompiler.parse(argv: Array<String>): ICompilerResult {
    val parseResult = CommandLineParser.parse(this, argv.asList())
    parseResult.invocation.flatten().use { invocations ->
        for (invocation in invocations) {
            val status = invocation.command.run()
            return status
        }
    }
    return ICompilerResult.CommonFailure(-1)
}

fun CliCompiler.main(argv: Array<String>): ICompilerResult {
    return CommandLineParser.mainReturningValue(this) { parse(argv) }
}