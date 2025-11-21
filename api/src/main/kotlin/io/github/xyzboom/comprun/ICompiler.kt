package io.github.xyzboom.comprun

import java.io.File

interface ICompiler {
    val languageId: String

    /**
     * For compilers that have unofficial implementations
     */
    val supplier: String
        get() = "Official"
    val version: String

    /**
     * Compile using specific files
     */
    fun compile(files: List<File>): ICompilerResult
    /**
     * Compile using specific file content.
     * @param files key: file name, value: content
     */
    fun compile(files: Map<String, String>): ICompilerResult
    /**
     * Compile using specific command line args
     */
    fun compile(args: Array<String>): ICompilerResult
}