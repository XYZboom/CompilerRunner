package io.github.xyzboom.comprun

interface ICompilerResult {
    val success: Boolean
    val exitCode: Int
    class CommonFailure(override val exitCode: Int) : ICompilerResult {
        override val success: Boolean = false
    }
    object CommonSuccess : ICompilerResult {
        override val success: Boolean = true
        override val exitCode: Int
            get() = 0
    }
}