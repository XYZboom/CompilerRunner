package io.github.xyzboom.comprun.kotlin

import io.github.xyzboom.comprun.ICompiler
import io.github.xyzboom.comprun.ICompilerResult
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CompilerSystemProperties
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.jvm.compiler.setupIdeaStandaloneExecution
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class KotlinCompiler(
    override val version: String
) : ICompiler {
    override val languageId: String
        get() = "kotlin"

    override fun compile(args: Array<String>, env: Map<String, String>): ICompilerResult {
        // We depend on swing (indirectly through PSI or something), so we want to declare headless mode,
        // to avoid accidentally starting the UI thread
        if (System.getProperty("java.awt.headless") == null) {
            System.setProperty("java.awt.headless", "true")
        }
        if (CompilerSystemProperties.KOTLIN_COLORS_ENABLED_PROPERTY.value == null) {
            CompilerSystemProperties.KOTLIN_COLORS_ENABLED_PROPERTY.value = "true"
        }

        setupIdeaStandaloneExecution()
        val output = ByteArrayOutputStream()
        val exitCode = PrintStream(output, true, Charsets.UTF_8).use { stream ->
            K2JVMCompiler().exec(stream, *args)
        }
        val message = output.toString(Charsets.UTF_8)
        return when (exitCode) {
            ExitCode.OK -> if (message.isEmpty()) {
                ICompilerResult.CommonSuccess
            } else {
                ICompilerResult.SuccessWithMessage(message)
            }
            else -> ICompilerResult.CommonFailure(exitCode.code, message)
        }
    }
}