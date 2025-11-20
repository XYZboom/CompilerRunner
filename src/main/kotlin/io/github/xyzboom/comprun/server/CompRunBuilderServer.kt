package io.github.xyzboom.comprun.server

import ch.epfl.scala.bsp4j.*
import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.xyzboom.comprun.CompRunInitData
import io.github.xyzboom.comprun.DefaultBuildTarget
import io.github.xyzboom.comprun.gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import java.util.concurrent.CompletableFuture

class CompRunBuilderServer : BuildServer {
    companion object {
        private val logger = KotlinLogging.logger {}
        const val SERVER_NAME = "CompilerRunnerServer"
        const val VERSION = "0.1.0-SNAPSHOT"
    }

    lateinit var client: BuildClient
    override fun buildInitialize(params: InitializeBuildParams): CompletableFuture<InitializeBuildResult> {
        // in CompilerRunner, the init data is used to download the compiler that runner needed.
        val initData = params.data as? JsonObject
        if (initData != null) {
            val initData = gson.fromJson(initData, CompRunInitData::class.java)
            println(initData)
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

    override fun buildShutdown(): CompletableFuture<in Any> {
        return CoroutineScope(Dispatchers.Default).async {}.asCompletableFuture()
    }

    override fun onBuildExit() {}

    override fun workspaceBuildTargets(): CompletableFuture<WorkspaceBuildTargetsResult> {
        return CoroutineScope(Dispatchers.Default).async {
            WorkspaceBuildTargetsResult(
                listOf(
                    BuildTarget(
                        BuildTargetIdentifier(DefaultBuildTarget.NAME),
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

    override fun buildTargetCompile(params: CompileParams?): CompletableFuture<CompileResult> {
        return CoroutineScope(Dispatchers.Default).async {
            if (params == null) {
                return@async CompileResult(StatusCode.ERROR)
            }
            // todo do compile here
            return@async CompileResult(StatusCode.ERROR)
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