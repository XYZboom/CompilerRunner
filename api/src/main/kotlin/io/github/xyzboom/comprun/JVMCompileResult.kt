package io.github.xyzboom.comprun

data class JVMCompileResult(
    val majorResult: String?,
    val javaResult: String?
) {
    val success: Boolean = majorResult == null && javaResult == null
}