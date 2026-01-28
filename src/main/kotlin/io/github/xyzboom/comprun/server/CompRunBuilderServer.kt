package io.github.xyzboom.comprun.server

import ch.epfl.scala.bsp4j.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.xyzboom.comprun.CompRunInitData
import io.github.xyzboom.comprun.Constants
import io.github.xyzboom.comprun.ICompiler
import io.github.xyzboom.comprun.ICompilerProvider
import io.github.xyzboom.comprun.ICompilerResult
import io.github.xyzboom.comprun.cli.CliCompiler
import io.github.xyzboom.comprun.cli.main
import io.github.xyzboom.comprun.gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import java.util.ServiceLoader
import java.util.concurrent.CompletableFuture
import kotlin.collections.groupBy

class CompRunBuilderServer : BuildServer {
    companion object {
        private val logger = KotlinLogging.logger {}
        const val SERVER_NAME = "CompilerRunnerServer"
        const val VERSION = "0.1.0-SNAPSHOT"

        // key1: languageId, key2: supplier
        private val providerMap: Map<String, Map<String, ICompilerProvider>>

        // key1: languageId, key2: supplier, key3: version
        private val compilers: MutableMap<String, MutableMap<String, MutableMap<String, ICompiler>>> = mutableMapOf()

        private fun getCompiler(languageId: String, supplier: String, version: String): ICompiler? {
            return compilers[languageId]?.get(supplier)?.get(version)
        }

        init {
            val providers: Iterable<ICompilerProvider> = ServiceLoader.load(ICompilerProvider::class.java)
            providerMap = providers.groupBy { it.languageId }.mapValues { it.value.associateBy { it1 -> it1.supplier } }
        }

        fun logProviders() {
            logger.info { "Available compiler providers:" }
            providerMap.forEach { (languageId, supplierMap) ->
                supplierMap.forEach { (supplier, _) ->
                    logger.info { "- Language: $languageId, Supplier: $supplier" }
                }
            }
        }
    }

    lateinit var client: BuildClient

    override fun buildInitialize(params: InitializeBuildParams): CompletableFuture<InitializeBuildResult> {
        // in CompilerRunner, the init data is used to download the compiler that runner needed.
        val initData = params.data as? JsonObject
        if (initData != null) {
            val initData = gson.fromJson(initData, CompRunInitData::class.java)
            val versionMap = initData.versionMap
            versionMap.forEach {
                it.value.forEach inner@{ it1 ->
                    val provider = providerMap[it.key]?.get(it1.key) ?: return@inner
                    it1.value.forEach { version ->
                        if (getCompiler(it.key, it1.key, version) != null) {
                            return@inner
                        }
                        val compiler = provider.requestCompiler(version) ?: return@inner
                        compilers.getOrPut(it.key) { mutableMapOf() }
                            .getOrPut(it1.key) { mutableMapOf() }[version] = compiler
                    }
                }
            }
        }
        return CoroutineScope(Dispatchers.Default).async {
            InitializeBuildResult(
                SERVER_NAME,
                VERSION,
                Bsp4j.PROTOCOL_VERSION,
                BuildServerCapabilities().apply {
                    // todo provide depends on what runner we have
                    compileProvider = CompileProvider(listOf("kotlin"))
                }
            )
        }.asCompletableFuture()
    }

    override fun onBuildInitialized() {
        logger.info { "Build initialized" }
    }

    override fun buildTargetCompile(params: CompileParams?): CompletableFuture<CompileResult> {
        return CoroutineScope(Dispatchers.Default).async {
            if (params == null) {
                return@async CompileResult(StatusCode.ERROR)
            }
            class Cli : CliCompiler() {
                val args by argument().multiple()
                override fun run(): ICompilerResult {
                    val compiler = compilers[language]?.get(supplier)?.get(version)
                        ?: return ICompilerResult.NoSuchCompiler(language, supplier, version)
                    return compiler.compile(args.toTypedArray(), env.associateBy(
                        keySelector = { it.substringBefore("=") },
                        valueTransform = { it.substringAfter("=") }
                    ))
                }
            }
            return@async try {
                val result = Cli().main(params.arguments.toTypedArray())
                val statusCode = if (result.success) {
                    StatusCode.OK
                } else {
                    StatusCode.ERROR
                }
                CompileResult(statusCode).apply {
                    data = result.toData()
                }
            } catch (e: Exception) {
                CompileResult(StatusCode.ERROR).apply {
                    data = ICompilerResult.CommonFailure(e).toData()
                }
            }
        }.asCompletableFuture()
    }

    override fun buildShutdown(): CompletableFuture<in Any> {
        return CoroutineScope(Dispatchers.Default).async {}.asCompletableFuture()
    }

    override fun onBuildExit() {}

    override fun workspaceBuildTargets(): CompletableFuture<WorkspaceBuildTargetsResult> {
        return CoroutineScope(Dispatchers.Default).async {
            WorkspaceBuildTargetsResult(
                listOf(
                    BuildTarget(
                        BuildTargetIdentifier(Constants.BUILD_TARGET_NAME),
                        emptyList(),
                        listOf("kotlin"),
                        emptyList(),
                        BuildTargetCapabilities().apply {
                            canCompile = true
                        }
                    )
                )
            )
        }.asCompletableFuture()
    }

    override fun workspaceReload(): CompletableFuture<in Any> {
        return CoroutineScope(Dispatchers.Default).async {}.asCompletableFuture()
    }

    override fun buildTargetSources(params: SourcesParams?): CompletableFuture<SourcesResult> {
        return CoroutineScope(Dispatchers.Default).async {
            SourcesResult(emptyList())
        }.asCompletableFuture()
    }

    override fun buildTargetInverseSources(params: InverseSourcesParams?): CompletableFuture<InverseSourcesResult> {
        return CoroutineScope(Dispatchers.Default).async {
            InverseSourcesResult(emptyList())
        }.asCompletableFuture()
    }

    override fun buildTargetDependencySources(params: DependencySourcesParams?): CompletableFuture<DependencySourcesResult> {
        return CoroutineScope(Dispatchers.Default).async {
            DependencySourcesResult(emptyList())
        }.asCompletableFuture()
    }

    override fun buildTargetDependencyModules(params: DependencyModulesParams?): CompletableFuture<DependencyModulesResult> {
        return CoroutineScope(Dispatchers.Default).async {
            DependencyModulesResult(emptyList())
        }.asCompletableFuture()
    }

    override fun buildTargetResources(params: ResourcesParams?): CompletableFuture<ResourcesResult> {
        return CoroutineScope(Dispatchers.Default).async {
            ResourcesResult(emptyList())
        }.asCompletableFuture()
    }

    override fun buildTargetOutputPaths(params: OutputPathsParams?): CompletableFuture<OutputPathsResult> {
        return CoroutineScope(Dispatchers.Default).async {
            OutputPathsResult(emptyList())
        }.asCompletableFuture()
    }

    override fun buildTargetRun(params: RunParams?): CompletableFuture<RunResult> {
        return CoroutineScope(Dispatchers.Default).async {
            RunResult(StatusCode.ERROR)
        }.asCompletableFuture()
    }

    override fun buildTargetTest(params: TestParams?): CompletableFuture<TestResult> {
        return CoroutineScope(Dispatchers.Default).async {
            TestResult(StatusCode.ERROR).apply {
                data = "Not supported"
            }
        }.asCompletableFuture()
    }

    override fun debugSessionStart(params: DebugSessionParams?): CompletableFuture<DebugSessionAddress?> {
        return CoroutineScope(Dispatchers.Default).async { null }.asCompletableFuture()
    }

    override fun buildTargetCleanCache(params: CleanCacheParams?): CompletableFuture<CleanCacheResult> {
        return CoroutineScope(Dispatchers.Default).async {
            // todo clean cache here
            CleanCacheResult(true)
        }.asCompletableFuture()
    }

    override fun onRunReadStdin(params: ReadParams?) {
    }

}