package io.github.xyzboom.comprun

interface ICompilerProvider {
    val languageId: String

    /**
     * For compilers that have unofficial implementations.
     * @see ICompiler.supplier
     */
    val supplier: String
        get() = "Official"

    fun requestCompiler(version: String): ICompiler?
}