package io.github.xyzboom.comprun.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

suspend fun downloadAndUnzip(
    url: String, targetDir: String
) = withContext(Dispatchers.IO) {
    val targetFile = File(targetDir)
    targetFile.mkdirs()

    val tempFile = File(targetDir, UUID.randomUUID().toString())

    try {
        val client = HttpClient(CIO)
        val statement = client.prepareGet(url) {
            timeout {
                connectTimeoutMillis = 10000
                requestTimeoutMillis = 10000
            }
        }
        statement.execute { response ->
            val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: 0L
            response.bodyAsChannel().let { channel ->
                tempFile.outputStream().use { outputStream ->
                    var totalBytesRead = 0L
                    val buffer = ByteArray(8192)

                    do {
                        val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                        if (bytesRead > 0) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            // 显示进度
                            if (contentLength > 0) {
                                val progress = (totalBytesRead * 100 / contentLength).toInt()
                                logger.info {
                                    "\rDownloading ${url.split("/").last()}: $progress% ($totalBytesRead/$contentLength bytes)"
                                }
                            }
                        }
                    } while (bytesRead > 0)

                    println() // 换行
                }
            }
        }

        client.close()
        unzipFile(tempFile, targetFile)
    } finally {
        tempFile.delete()
    }
}

fun unzipFile(zipFile: File, targetDir: File) {
    ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
        var entry: ZipEntry?
        while (zipInputStream.nextEntry.also { entry = it } != null) {
            val entryName = entry!!.name
            val outputFile = File(targetDir, entryName)
            if (entry.isDirectory) {
                outputFile.mkdirs()
            } else {
                outputFile.parentFile.mkdirs()

                FileOutputStream(outputFile).use { output ->
                    zipInputStream.copyTo(output)
                }
            }
            zipInputStream.closeEntry()
        }
    }
}