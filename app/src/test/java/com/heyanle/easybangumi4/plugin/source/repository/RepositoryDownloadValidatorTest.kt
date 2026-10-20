package com.heyanle.easybangumi4.plugin.source.repository

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class RepositoryDownloadValidatorTest {
    private fun withSource(block: (File) -> Unit) {
        val file = File.createTempFile("repository-download-", ".js")
        try {
            file.writeText("// @key source\n// @label Source\n// @versionName 1.0\n// @versionCode 100\n// @libVersion 7\nthrow new Error('must never execute when inspecting');")
            block(file)
        } finally {
            file.delete()
        }
    }

    @Test
    fun acceptsMatchingMetadataWithoutExecutingScript() = withSource { file ->
        validateRepositoryDownload(RepositoryEntry("source", "Source", versionCode = 100), file)
    }

    @Test
    fun rejectsDifferentSourceOrStaleVersionBeforeInstallation() = withSource { file ->
        listOf(
            RepositoryEntry("other", "Other", versionCode = 100),
            RepositoryEntry("source", "Source", versionCode = 101),
        ).forEach { entry ->
            assertTrue(runCatching { validateRepositoryDownload(entry, file) }.exceptionOrNull() is IllegalArgumentException)
        }
    }

    @Test
    fun preservesLegacyCustomCatalogueWithoutVersionCode() = withSource { file ->
        validateRepositoryDownload(RepositoryEntry("source", "Source"), file)
    }

    @Test
    fun verifiesHashWhenCatalogueDeclaresOne() = withSource { file ->
        val hash = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        val entry = RepositoryEntry("source", "Source", versionCode = 100, sha256 = hash.uppercase())
        validateRepositoryDownload(entry, file)
        assertTrue(runCatching { validateRepositoryDownload(entry.copy(sha256 = "0".repeat(64)), file) }.isFailure)
    }
}
