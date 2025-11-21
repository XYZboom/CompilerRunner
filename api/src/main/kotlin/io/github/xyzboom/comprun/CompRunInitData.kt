package io.github.xyzboom.comprun

data class CompRunInitData @JvmOverloads constructor(
    /**
     * Key: Language id
     * Value: a map that:
     *     Key: Compiler Supplier, default is [Constants.DEFAULT_SUPPLIER]
     *     Value: Compiler versions need to download
     */
    val versionMap: Map<String, Map<String, Set<String>>> = emptyMap(),
    val outputPath: String = "out"
)