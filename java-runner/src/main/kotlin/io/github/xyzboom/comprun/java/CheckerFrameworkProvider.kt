package io.github.xyzboom.comprun.java

import io.github.xyzboom.comprun.Constants
import io.github.xyzboom.comprun.ICompiler
import io.github.xyzboom.comprun.ICompilerProvider
import io.github.xyzboom.comprun.utils.downloadAndUnzip
import kotlinx.coroutines.runBlocking
import java.io.File

class CheckerFrameworkProvider : ICompilerProvider {
    override val languageId: String
        get() = "java"

    override val supplier: String
        get() = "CheckerFramework"

    override fun requestCompiler(version: String): ICompiler {
        val url = "https://github.com/typetools/checker-framework/releases/download/" +
                "checker-framework-${version}/checker-framework-${version}.zip"
        val targetDir = "${Constants.CRHome}/java/${supplier}/$version"
        val checkerDir = File(targetDir, "checker-framework-${version}/checker")
        val targetExists = File(checkerDir, "dist/checker.jar").exists()
        if (!targetExists) {
            runBlocking {
                downloadAndUnzip(url, targetDir)
            }
        }

        val scriptFile = File(checkerDir, "bin/javac")
        scriptFile.setExecutable(true)

        return CheckerFramework(scriptFile.absolutePath, version)
    }
}