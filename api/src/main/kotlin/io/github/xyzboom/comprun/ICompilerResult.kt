package io.github.xyzboom.comprun

interface ICompilerResult {
    val success: Boolean
    val message: String? get() = null
    val exitCode: Int

    companion object {
        class NoSuchCompiler(
            language: String,
            supplier: String,
            version: String
        ) : CommonFailure(-1, "no such compiler: $language $supplier, version: $version")
    }

    open class CommonFailure(
        override val exitCode: Int,
        override val message: String? = null
    ) : ICompilerResult {
        override val success: Boolean = false
    }

    object CommonSuccess : ICompilerResult {
        override val success: Boolean = true
        override val exitCode: Int
            get() = 0
    }
}