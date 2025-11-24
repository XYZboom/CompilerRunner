package io.github.xyzboom.comprun.utils

import java.net.URL
import java.net.URLClassLoader

/**
 * Find classes in [packageNames] first.
 * Other classes are loaded by [parent] first.
 */
class InternalFindFirstClassLoader(
    urls: Array<URL>, parent: ClassLoader?,
    private val packageNames: List<String>
) : URLClassLoader(urls, parent) {
    override fun loadClass(name: String, resolve: Boolean): Class<*>? {
        if (packageNames.any { name.startsWith(it) }) {
            synchronized(getClassLoadingLock(name)) {
                val loadedClass = findLoadedClass(name)
                if (loadedClass != null) {
                    return loadedClass
                }
                val tryFind = try {
                    findClass(name)
                } catch (_: ClassNotFoundException) {
                    null
                }
                if (tryFind != null) {
                    resolveClass(tryFind)
                    return tryFind
                }
            }
        }
        return super.loadClass(name, resolve)
    }
}