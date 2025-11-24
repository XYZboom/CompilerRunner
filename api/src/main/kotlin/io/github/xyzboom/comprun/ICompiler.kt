package io.github.xyzboom.comprun

interface ICompiler {
    val languageId: String

    /**
     * For compilers that have unofficial implementations
     */
    val supplier: String
        get() = "Official"
    val version: String
    /**
     * Compile using specific command line args
     */
    fun compile(args: Array<String>): ICompilerResult
}