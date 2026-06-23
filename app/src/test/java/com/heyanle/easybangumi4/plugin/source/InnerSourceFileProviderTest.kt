package com.heyanle.easybangumi4.plugin.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import kotlin.io.path.createTempDirectory

class InnerSourceFileProviderTest {

    @Test
    fun loadSourceFilesCopiesOnlyJsAssets() {
        val cacheFolder = createTempDirectory("inner_source_cache").toFile()
        try {
            File(cacheFolder, "old.js").writeText("old")
            val provider = InnerSourceFileProvider(
                assetReader = MemoryAssetReader(
                    mapOf(
                        "inner_source/source.js" to "js",
                        "inner_source/nested/source.txt" to "ignored nested",
                        "inner_source/readme.txt" to "ignored",
                    )
                ),
                cacheFolder = cacheFolder,
            )

            val files = provider.loadSourceFiles()

            assertEquals(
                listOf("source.js"),
                files.map { it.relativeTo(cacheFolder).invariantSeparatorsPath }.sorted(),
            )
            assertEquals("js", File(cacheFolder, "source.js").readText())
            assertFalse(File(cacheFolder, "nested/source.txt").exists())
            assertFalse(File(cacheFolder, "readme.txt").exists())
            assertFalse(File(cacheFolder, "old.js").exists())
        } finally {
            cacheFolder.deleteRecursively()
        }
    }

    @Test
    fun loadSourceFilesReturnsEmptyWhenInnerSourceAssetIsMissing() {
        val cacheFolder = createTempDirectory("inner_source_empty").toFile()
        try {
            val provider = InnerSourceFileProvider(
                assetReader = MemoryAssetReader(emptyMap()),
                cacheFolder = cacheFolder,
            )

            assertTrue(provider.loadSourceFiles().isEmpty())
        } finally {
            cacheFolder.deleteRecursively()
        }
    }

    private class MemoryAssetReader(
        private val files: Map<String, String>,
    ) : InnerSourceFileProvider.AssetReader {
        override fun list(path: String): List<String> {
            val prefix = "$path/"
            val children = files.keys
                .filter { it.startsWith(prefix) }
                .map { it.removePrefix(prefix).substringBefore("/") }
                .distinct()
            if (children.isEmpty() && path !in files) {
                throw java.io.IOException("missing asset: $path")
            }
            return children
        }

        override fun open(path: String): InputStream {
            return ByteArrayInputStream(files.getValue(path).toByteArray())
        }
    }
}
