package io.github.xyzboom.comprun.client

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.DidChangeBuildTarget
import ch.epfl.scala.bsp4j.LogMessageParams
import ch.epfl.scala.bsp4j.PrintParams
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.ShowMessageParams
import ch.epfl.scala.bsp4j.TaskFinishParams
import ch.epfl.scala.bsp4j.TaskProgressParams
import ch.epfl.scala.bsp4j.TaskStartParams

class CompRunBuildClient: BuildClient {
    companion object {
        const val CLIENT_NAME = "CompilerRunnerClient"
        const val CLIENT_VERSION = "0.1.0-SNAPSHOT"
    }
    override fun onBuildShowMessage(params: ShowMessageParams?) {
    }

    override fun onBuildLogMessage(params: LogMessageParams?) {
    }

    override fun onBuildPublishDiagnostics(params: PublishDiagnosticsParams?) {
    }

    override fun onBuildTargetDidChange(params: DidChangeBuildTarget?) {
    }

    override fun onBuildTaskStart(params: TaskStartParams?) {
    }

    override fun onBuildTaskProgress(params: TaskProgressParams?) {
    }

    override fun onBuildTaskFinish(params: TaskFinishParams?) {
    }

    override fun onRunPrintStdout(params: PrintParams?) {
    }

    override fun onRunPrintStderr(params: PrintParams?) {
    }

}