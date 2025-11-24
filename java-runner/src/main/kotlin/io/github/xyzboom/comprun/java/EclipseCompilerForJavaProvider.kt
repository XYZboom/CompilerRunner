package io.github.xyzboom.comprun.java

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.github.xyzboom.comprun.ICompiler
import io.github.xyzboom.comprun.ICompilerProvider
import io.github.xyzboom.comprun.maven.MavenDependencyResolver
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.resolution.DependencyResolutionException
import java.lang.reflect.Constructor
import java.net.URLClassLoader

class EclipseCompilerForJavaProvider : ICompilerProvider {
    companion object {
        private val cache = hashMapOf<String, Triple<URLClassLoader, Class<ICompiler>, Constructor<ICompiler>>>()

        class Cli : CliktCommand() {
            val version by option("--version", "-v", help = "version of compiler to run").default("2.2.21")
            val args by argument().multiple()

            override fun run() {
                val provider = EclipseCompilerForJavaProvider()
                val compiler = provider.requestCompiler(version)
                compiler!!.compile(args.toTypedArray())
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            Cli().main(args)
        }
    }

    override val languageId: String
        get() = "java"
    override val supplier: String
        get() = "ECJ"

    override fun requestCompiler(version: String): ICompiler? {
        val cacheResult = cache[version]
        if (cacheResult != null) {
            return cacheResult.third.newInstance(version)
        }
        val resolver = MavenDependencyResolver()
        val ecjArtifact = DefaultArtifact("org.eclipse.jdt.core.compiler:ecj:$version")
        val files = try {
            resolver.resolveWithDependencies(ecjArtifact)
        } catch (_: DependencyResolutionException) {
            return null
        }
        val compilerClasses = listOf(EclipseCompilerForJava::class, ICompiler::class)
        val classLocations = compilerClasses.map { it.java.protectionDomain.codeSource.location }
        val classpath = files.map { it.toURI().toURL() } /*+ classLocations*/
        /*
        val loader = InternalFindFirstClassLoader((files.map { it.toURI().toURL() } + classLocations).toTypedArray(),
            null,
            listOf(ICompiler::class.java.`package`.name, ""))

        @Suppress("UNCHECKED_CAST")
        val compilerClass = loader.loadClass(EclipseCompilerForJava::class.qualifiedName) as Class<ICompiler>
        val constructor = compilerClass.getConstructor(String::class.java)
        cache[version] = Triple(loader, compilerClass, constructor)*/
//        val compiler = LoopedJvmProcessCompiler(
//            classpath, EclipseCompilerForJava::class.qualifiedName,
//        )

        return EclipseCompilerForJava(classpath, version)
    }

}