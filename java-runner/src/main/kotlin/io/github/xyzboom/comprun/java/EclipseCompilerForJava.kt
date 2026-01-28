package io.github.xyzboom.comprun.java

import io.github.xyzboom.comprun.ICompiler
import io.github.xyzboom.comprun.ICompilerResult
import io.github.xyzboom.comprun.utils.InternalFindFirstClassLoader
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.net.URL

class EclipseCompilerForJava(
    classpath: List<URL>,
    override val version: String
) : ICompiler {
    private val out = StringWriter()
    private val err = StringWriter()
    val mainConstructor: Constructor<*>
    val compileMethod: Method

    init {
        val loader = InternalFindFirstClassLoader(classpath.toTypedArray(), null, listOf("org.eclipse"))

        @Suppress("UNCHECKED_CAST")
        val mainClass = loader.loadClass("org.eclipse.jdt.internal.compiler.batch.Main")
        val printWriterClass = loader.loadClass(PrintWriter::class.qualifiedName)
        mainConstructor = mainClass.getConstructor(
            printWriterClass, printWriterClass,
            Boolean::class.javaPrimitiveType, loader.loadClass(Map::class.java.canonicalName),
            loader.loadClass("org.eclipse.jdt.core.compiler.CompilationProgress")
        )
        compileMethod = mainClass.getMethod("compile", Array<String>::class.java)
    }

    override val languageId: String
        get() = "java"

    override fun compile(args: Array<String>, env: Map<String, String>): ICompilerResult {
        val main = mainConstructor.newInstance(
            PrintWriter(out),
            PrintWriter(err),
            false,
            null,
            null,
        )
        val compileResult = compileMethod.invoke(main, args) as? Boolean
        val outString = out.toString()
        val errorString = err.toString()
        println(outString)
        System.err.println(errorString)
        err.buffer.setLength(0)
        out.buffer.setLength(0)
        return if (compileResult == false) {
            ICompilerResult.CommonFailure(-1, outString + errorString)
        } else ICompilerResult.SuccessWithMessage(outString + errorString)
    }
}