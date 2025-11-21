package io.github.xyzboom.comprun

data class JVMCompileResult(
    val majorResult: String?,
    val javaResult: String?,
    override val exitCode: Int = 0
): ICompilerResult {
    override val success: Boolean = majorResult == null && javaResult == null
}