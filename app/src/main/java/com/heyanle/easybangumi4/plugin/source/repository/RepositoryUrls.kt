package com.heyanle.easybangumi4.plugin.source.repository

import java.net.URI

internal fun isRepositoryUrlValid(url: String): Boolean = runCatching {
    val uri = URI(url)
    uri.scheme?.lowercase() in setOf("http", "https") &&
        !uri.host.isNullOrBlank() && uri.userInfo == null && uri.fragment == null
}.getOrDefault(false)

/** Only explicitly saved repositories are loaded; no catalogue is added implicitly. */
internal fun activeRepositories(repositories: List<RepositoryInfo>): List<RepositoryInfo> =
    repositories.map { it.copy(url = it.url.trim()) }.distinctBy { it.url }
