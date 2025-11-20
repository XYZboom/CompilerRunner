package io.github.xyzboom.comprun.server

import ch.epfl.scala.bsp4j.*
import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.xyzboom.comprun.CompRunInitData
import io.github.xyzboom.comprun.gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import java.util.concurrent.CompletableFuture

class CompRunBuilderServer: BuildServer {
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
                BuildServerCapabilities()
            )
        }.asCompletableFuture()
    }

    override fun onBuildInitialized() {
        logger.info { "Build initialized" }
    }

    override fun buildShutdown(): CompletableFuture<in Any> {
        TODO("Not yet implemented")
    }

    override fun onBuildExit() {
        TODO("Not yet implemented")
    }

    override fun workspaceBuildTargets(): CompletableFuture<WorkspaceBuildTargetsResult?>? {
        TODO("Not yet implemented")
    }

    override fun workspaceReload(): CompletableFuture<in Any>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetSources(params: SourcesParams?): CompletableFuture<SourcesResult?>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetInverseSources(params: InverseSourcesParams?): CompletableFuture<InverseSourcesResult?>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetDependencySources(params: DependencySourcesParams?): CompletableFuture<DependencySourcesResult?>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetDependencyModules(params: DependencyModulesParams?): CompletableFuture<DependencyModulesResult?>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetResources(params: ResourcesParams?): CompletableFuture<ResourcesResult?>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetOutputPaths(params: OutputPathsParams?): CompletableFuture<OutputPathsResult?>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetCompile(params: CompileParams?): CompletableFuture<CompileResult?>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetRun(params: RunParams?): CompletableFuture<RunResult?>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetTest(params: TestParams?): CompletableFuture<TestResult?>? {
        TODO("Not yet implemented")
    }

    override fun debugSessionStart(params: DebugSessionParams?): CompletableFuture<DebugSessionAddress?>? {
        TODO("Not yet implemented")
    }

    override fun buildTargetCleanCache(params: CleanCacheParams?): CompletableFuture<CleanCacheResult?>? {
        TODO("Not yet implemented")
    }

    override fun onRunReadStdin(params: ReadParams?) {
        TODO("Not yet implemented")
    }

}