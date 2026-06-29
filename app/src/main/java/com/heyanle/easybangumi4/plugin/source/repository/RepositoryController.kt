package com.heyanle.easybangumi4.plugin.source.repository

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.KtorUtil
import com.heyanle.easybangumi4.utils.downloadTo
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.jsonTo
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RepositoryController(
    private val sourceController: SourceController,
    private val repositoryPreferences: RepositoryPreferences,
) {
    sealed class State {
        data object Loading : State()
        data class Loaded(val entries: List<RepositoryEntry>) : State()
        data class Error(val message: String) : State()
    }

    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.newSingleDispatcher)

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state

    private val _repoCount = MutableStateFlow(0)
    val repoCount: StateFlow<Int> = _repoCount

    init {
        scope.launch {
            repositoryPreferences.repositories.requestFlow
                .distinctUntilChanged()
                .collectLatest { repos ->
                    _repoCount.update { repos.size }
                    if (repos.isEmpty()) {
                        _state.update { State.Loaded(emptyList()) }
                    } else {
                        _state.update { State.Loading }
                        refreshEntries(repos)
                    }
                }
        }
    }

    fun refresh() {
        scope.launch {
            val repos = repositoryPreferences.repositories.getOrDef()
            _state.update { State.Loading }
            refreshEntries(repos)
        }
    }

    private suspend fun refreshEntries(repos: List<RepositoryInfo>) {
        try {
            val allEntries = repos.map { repo ->
                scope.async(Dispatchers.IO) {
                    fetchRepoEntries(repo)
                }
            }.awaitAll().flatten()
            _state.update { State.Loaded(allEntries) }
        } catch (e: Exception) {
            _state.update { State.Error(e.message ?: "Failed to load repositories") }
        }
    }

    private suspend fun fetchRepoEntries(repo: RepositoryInfo): List<RepositoryEntry> {
        return withContext(Dispatchers.IO) {
            try {
                val response = KtorUtil.client.get(repo.url)
                val body: String = response.body()
                body.lineSequence()
                    .filter { it.isNotBlank() }
                    .mapNotNull { line ->
                        line.jsonTo<RepositoryEntry>()?.also { it.repoUrl = repo.url }
                    }
                    .toList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Install a source from a repository entry.
     * Downloads the .js file and installs via SourceController.
     */
    suspend fun installSource(entry: RepositoryEntry): DataResult<Unit> {
        if (entry.url.isBlank()) {
            return DataResult.error("Source download URL is empty")
        }
        return try {
            val cacheDir = File(APP.getCachePath("source_v3_repo"))
            cacheDir.mkdirs()
            val fileName = entry.key + ".js"
            val targetFile = File(cacheDir, fileName)

            entry.url.downloadTo(targetFile.absolutePath)

            if (!targetFile.exists() || targetFile.length() == 0L) {
                return DataResult.error("Download failed")
            }

            val result = sourceController.appendOrUpdateSource(targetFile)
            result.map { Unit }
        } catch (e: Exception) {
            DataResult.error(e.message ?: "Install failed", e)
        }
    }
}