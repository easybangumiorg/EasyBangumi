package com.heyanle.easybangumi4.plugin.source.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryIndexParserTest {
    private val adapter = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        .adapter(RepositoryEntry::class.java)
    private val entry = """{"key":"source","label":"Source","version":"1.0","versionCode":1,"url":"https://example.org/source.js"}"""

    private fun parse(body: String) = parseRepositoryIndex(body, "https://example.org/index.jsonl", adapter::fromJson)

    @Test
    fun readsJsonlWithBomAndBlankLinesAndAttachesOrigin() {
        val entries = parse("\uFEFF\n$entry\n\n")
        assertEquals("source", entries.single().key)
        assertEquals("https://example.org/index.jsonl", entries.single().repoUrl)
        assertTrue(parse("\n\n").isEmpty())
    }

    @Test
    fun rejectsHtmlAndMalformedRecordsRatherThanReturningEmptyCatalogue() {
        listOf("<html>404</html>", "{}", "null", "{bad json}", entry.replace("https://example.org/source.js", "file:///tmp/source.js")).forEach { body ->
            assertTrue(body, runCatching { parse(body) }.exceptionOrNull() is IllegalArgumentException)
        }
    }

    @Test
    fun rejectsDuplicateKeysToAvoidDuplicateComposeRows() {
        val error = runCatching { parse("$entry\n$entry") }.exceptionOrNull()
        assertTrue(error?.message.orEmpty().contains("重复"))
    }

    @Test
    fun readsOfficialVersionNameAndOptionalHash() {
        val entries = parse(entry.replace("\"version\":\"1.0\"", "\"versionName\":\"1.0\",\"sha256\":\"${"a".repeat(64)}\""))
        assertEquals("1.0", entries.single().displayVersion)
        assertEquals("a".repeat(64), entries.single().sha256)
        assertTrue(runCatching { parse(entry.dropLast(1) + ",\"sha256\":\"invalid\"}") }.isFailure)
    }
}
