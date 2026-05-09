package org.easybangumi.next.shared.download.platform.desktop

import java.io.File

/**
 * Desktop 端 FFmpeg 合并器
 */
object FfmpegMerger {

    fun isAvailable(): Boolean {
        return try {
            val process = ProcessBuilder("ffmpeg", "-version")
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun merge(
        tsFilePaths: List<String>,
        outputFilePath: String,
        onProgress: (Float) -> Unit = {},
    ): Boolean {
        if (tsFilePaths.isEmpty()) return false

        val outputFile = File(outputFilePath)
        outputFile.parentFile?.mkdirs()

        val listFile = File(outputFile.parentFile, "filelist.txt")
        listFile.writeText(tsFilePaths.joinToString("\n") { "file '$it'" })

        try {
            val process = ProcessBuilder(
                "ffmpeg", "-y",
                "-f", "concat",
                "-safe", "0",
                "-i", listFile.absolutePath,
                "-c", "copy",
                outputFilePath
            )
                .redirectErrorStream(true)
                .start()

            val inputStream = process.inputStream
            val buffer = ByteArray(1024)
            while (inputStream.read(buffer) != -1) {
            }

            val exitCode = process.waitFor()
            return exitCode == 0
        } finally {
            listFile.delete()
        }
    }
}
