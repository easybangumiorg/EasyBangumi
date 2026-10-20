package com.heyanle.easybangumi4.plugin.source.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryUrlsTest {
    @Test
    fun emptySavedListDoesNotProvideAnyRepository() {
        assertTrue(activeRepositories(emptyList()).isEmpty())
    }

    @Test
    fun preservesExplicitlyAddedRepositoriesIncludingFormerOfficialAddresses() {
        val release = RepositoryInfo("https://easybangumi.org/repository/v2/index.jsonl")
        val dev = RepositoryInfo("https://raw.githubusercontent.com/easybangumiorg/CommunityJsExtensionEasyBangumi/main/channel/dev/index.jsonl")
        val custom = RepositoryInfo("https://example.org/index.jsonl", "My repository", addedAt = 10)
        val stored = listOf(release, custom, dev, custom.copy(url = " ${custom.url} "))
        assertEquals(listOf(release, custom, dev), activeRepositories(stored))
        // Computing the effective list must not rewrite the user's original saved list.
        assertEquals(4, stored.size)
    }

    @Test
    fun doesNotAddOrReplaceAUserSelectedCatalogue() {
        val repository = RepositoryInfo("https://example.org/index.jsonl", "My repository")
        assertEquals(listOf(repository), activeRepositories(listOf(repository)))
    }

    @Test
    fun acceptsHttpEndpointsAndRejectsNonNetworkAddresses() {
        assertTrue(isRepositoryUrlValid("https://example.org/index.jsonl?channel=dev"))
        assertTrue(isRepositoryUrlValid("http://192.168.1.2:8080/catalogue"))
        listOf("file:///tmp/index.jsonl", "javascript:alert(1)", "https://", "https://example.org/a b", "https://user:password@example.org/", "https://example.org/#fragment").forEach {
            assertFalse(it, isRepositoryUrlValid(it))
        }
    }
}
