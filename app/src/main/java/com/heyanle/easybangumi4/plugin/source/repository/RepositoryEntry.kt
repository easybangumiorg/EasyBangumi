package com.heyanle.easybangumi4.plugin.source.repository

/**
 * Represents a single source entry in a repository's index.jsonl.
 * Each line in the file is a JSON object with these fields.
 */
data class RepositoryEntry(
    val key: String,          // unique source key
    val label: String,        // display name
    val version: String = "", // version string
    val versionCode: Long = 0,
    val url: String = "",     // download URL for the .js source file
    val describe: String? = null,
) {
    // repository this entry came from (transient, not serialized by Moshi)
    var repoUrl: String = ""
}

/**
 * A repository is a URL pointing to an index.jsonl file.
 */
data class RepositoryInfo(
    val url: String,
    val label: String = "",   // user-defined display name
    val addedAt: Long = System.currentTimeMillis(),
)
