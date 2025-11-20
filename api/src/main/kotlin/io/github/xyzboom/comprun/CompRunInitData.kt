package io.github.xyzboom.comprun

data class CompRunInitData(
    /**
     * Key: Language id
     * Value: Compiler versions need to download
     */
    val versionMap: Map<String, Set<String>> = emptyMap(),
)