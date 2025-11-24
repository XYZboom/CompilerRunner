package io.github.xyzboom.comprun

interface ICompilerResult {
    val success: Boolean
    val message: String? get() = null
    val exitCode: Int

    fun toData(): DataCompilerResult {
        return DataCompilerResult(success, message, exitCode)
    }

    class NoSuchCompiler(
        language: String,
        supplier: String,
        version: String
    ) : CommonFailure(-1, "no such compiler: $language $supplier, version: $version")

    data class DataCompilerResult(
        override val success: Boolean,
        override val message: String?,
        override val exitCode: Int
    ) : ICompilerResult {
        override fun toData(): DataCompilerResult {
            return this
        }
    }

    open class CommonFailure(
        override val exitCode: Int,
        override val message: String? = null
    ) : ICompilerResult {
        constructor(exception: Throwable) : this(-1, exception.stackTraceToString())
        override val success: Boolean = false
    }

    object CommonSuccess : ICompilerResult {
        override val success: Boolean = true
        override val exitCode: Int
            get() = 0
    }
}