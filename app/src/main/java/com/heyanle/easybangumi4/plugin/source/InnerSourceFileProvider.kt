package com.heyanle.easybangumi4.plugin.source

import android.app.Application
import java.io.File
import java.io.IOException
import java.io.InputStream

class InnerSourceFileProvider(
    private val assetReader: AssetReader,
    private val cacheFolder: File,
) {
    constructor(
        application: Application,
        cacheFolder: File,
    ) : this(AndroidAssetReader(application), cacheFolder)

    fun loadSourceFiles(): List<File> {
        val allAssets = collectAssets(ASSET_DIR).sorted()
        val sourceAssets = allAssets
            .filter { it.endsWith(PluginV3.JS_SOURCE_SUFFIX) }

        cacheFolder.mkdirs()
        clearStaleFiles(allAssets.mapTo(hashSetOf()) { it.removePrefix("$ASSET_DIR/") })

        return sourceAssets.mapNotNull { assetPath ->
            val relativePath = assetPath.removePrefix("$ASSET_DIR/")
            val target = File(cacheFolder, relativePath)
            runCatching {
                target.parentFile?.mkdirs()
                assetReader.open(assetPath).use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                target
            }.getOrNull()
        }
    }

    private fun collectAssets(path: String): List<String> {
        val children = try {
            assetReader.list(path)
        } catch (_: IOException) {
            return emptyList()
        }
        if (children.isEmpty()) {
            return listOf(path)
        }
        return children.flatMap { child ->
            collectAssets("$path/$child")
        }
    }

    private fun clearStaleFiles(expectedRelativePaths: Set<String>) {
        if (!cacheFolder.exists()) {
            return
        }
        cacheFolder.walkBottomUp().forEach { file ->
            if (file.isFile) {
                val relativePath = file.relativeTo(cacheFolder).invariantSeparatorsPath
                if (relativePath !in expectedRelativePaths) {
                    file.delete()
                }
            } else if (file != cacheFolder && file.listFiles().isNullOrEmpty()) {
                file.delete()
            }
        }
    }

    private companion object {
        const val ASSET_DIR = "inner_source"
    }

    interface AssetReader {
        @Throws(IOException::class)
        fun list(path: String): List<String>

        @Throws(IOException::class)
        fun open(path: String): InputStream
    }

    private class AndroidAssetReader(
        private val application: Application,
    ) : AssetReader {
        override fun list(path: String): List<String> {
            return application.assets.list(path).orEmpty().toList()
        }

        override fun open(path: String): InputStream {
            return application.assets.open(path)
        }
    }
}
