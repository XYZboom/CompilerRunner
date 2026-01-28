package io.github.xyzboom.comprun.java

import io.github.xyzboom.comprun.ICompiler
import io.github.xyzboom.comprun.ICompilerResult

class CheckerFramework(
    val mainScript: String,
    override val version: String
) : ICompiler {
    override val languageId: String
        get() = "java"

    override fun compile(args: Array<String>, env: Map<String, String>): ICompilerResult {
        val process = Runtime.getRuntime().exec(
            "$mainScript ${args.joinToString(" ")}",
            env.map { (k, v) -> "$k=$v" }.toTypedArray(),
        )
        val exitCode = process.waitFor()
        val output = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()
        val combinedOutput = output + error
        return if (exitCode != 0) {
            ICompilerResult.CommonFailure(exitCode, combinedOutput)
        } else {
            ICompilerResult.SuccessWithMessage(combinedOutput)
        }
    }
}