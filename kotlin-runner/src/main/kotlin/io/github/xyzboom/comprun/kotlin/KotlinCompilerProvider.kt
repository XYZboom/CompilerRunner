package io.github.xyzboom.comprun.kotlin

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.github.xyzboom.comprun.Constants
import io.github.xyzboom.comprun.ICompiler
import io.github.xyzboom.comprun.ICompilerProvider
import io.github.xyzboom.comprun.utils.downloadAndUnzip
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.reflect.Constructor
import java.net.URLClassLoader

class KotlinCompilerProvider : ICompilerProvider {
    override val languageId: String
        get() = "kotlin"


    companion object {
        private val cache = hashMapOf<String, Triple<URLClassLoader, Class<ICompiler>, Constructor<ICompiler>>>()

        class Cli : CliktCommand() {
            val version by option("--version", "-v", help = "version of compiler to run").default("2.2.21")
            val args by argument().multiple()

            override fun run() {
                val provider = KotlinCompilerProvider()
                val compiler = provider.requestCompiler(version)
                compiler!!.compile(args.toTypedArray())
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            Cli().main(args)
        }
    }

    @Synchronized
    override fun requestCompiler(version: String): ICompiler? {
        val cacheResult = cache[version]
        if (cacheResult != null) {
            return cacheResult.third.newInstance(version)
        }
        val url = "https://github.com/JetBrains/kotlin/releases/download/v$version/kotlin-compiler-$version.zip"
        val targetDir = "${Constants.CRHome}/kotlin/${supplier}/$version"
        val targetExists = File(targetDir, "kotlinc/bin/kotlinc").exists()
        if (!targetExists) {
            runBlocking {
                downloadAndUnzip(url, targetDir)
            }
        }

        val files: Array<File> = File(targetDir, "kotlinc/lib").listFiles()
        val classLocation = KotlinCompiler::class.java.protectionDomain.codeSource.location

        val loader = object : URLClassLoader((files.map { it.toURI().toURL() } + classLocation).toTypedArray(),
            this::class.java.classLoader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*>? {
                if (name.startsWith(this::class.java.`package`.name)) {
                    val tryFind = findClass(name)
                    if (tryFind != null) {
                        return tryFind
                    }
                }
                return super.loadClass(name, resolve)
            }
        }

        @Suppress("UNCHECKED_CAST")
        val compilerClass = loader.loadClass("io.github.xyzboom.comprun.kotlin.KotlinCompiler") as Class<ICompiler>
        val constructor = compilerClass.getConstructor(String::class.java)
        cache[version] = Triple(loader, compilerClass, constructor)
        return constructor.newInstance(version) as ICompiler
    }

}

