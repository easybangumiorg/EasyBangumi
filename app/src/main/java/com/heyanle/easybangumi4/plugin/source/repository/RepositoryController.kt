package com.heyanle.easybangumi4.plugin.source.repository

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.KtorUtil
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.jsonTo
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

class RepositoryController(
    private val sourceController: SourceController,
    private val repositoryPreferences: RepositoryPreferences,
) {
    sealed class State {
        data object Loading : State()
        data class Loaded(val entries: List<RepositoryEntry>, val warnings: List<String> = emptyList()) : State()
        data class Error(val message: String) : State()
    }

    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.newSingleDispatcher)

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state

    private val _repoCount = MutableStateFlow(0)
    val repoCount: StateFlow<Int> = _repoCount
    private val refreshTrigger = MutableStateFlow(0L)

    init {
        scope.launch {
            combine(repositoryPreferences.activeRepositories, refreshTrigger) { repos, _ -> repos }
                .collectLatest { repos ->
                    _repoCount.update { repos.size }
                    _state.update { State.Loading }
                    refreshEntries(repos)
                }
        }
    }

    fun refresh() {
        refreshTrigger.update { it + 1 }
    }

    private suspend fun refreshEntries(repos: List<RepositoryInfo>) {
        try {
            val results = coroutineScope {
                repos.map { repo ->
                    async(Dispatchers.IO) { fetchRepoEntries(repo) }
                }.awaitAll()
            }
            val failures = results.mapNotNull { it.exceptionOrNull()?.message }
            val successful = results.mapNotNull { it.getOrNull() }
            _state.update {
                if (successful.isEmpty() && failures.isNotEmpty()) {
                    State.Error(failures.joinToString("\n"))
                } else {
                    State.Loaded(successful.flatten(), failures)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { State.Error(e.message ?: "Failed to load repositories") }
        }
    }

    private suspend fun fetchRepoEntries(repo: RepositoryInfo): Result<List<RepositoryEntry>> {
        return withContext(Dispatchers.IO) {
            try {
                val entries = withTimeout(30_000) {
                    val response = KtorUtil.client.get(repo.url)
                    check(response.status.value in 200..299) { "HTTP ${response.status.value}" }
                    val body: String = response.body()
                    parseRepositoryIndex(body, repo.url) { it.jsonTo<RepositoryEntry>() }
                }
                Result.success(entries)
            } catch (e: CancellationException) {
                // A channel switch must cancel the previous catalogue request.
                if (e is kotlinx.coroutines.TimeoutCancellationException) {
                    Result.failure(IllegalStateException("${repo.label.ifBlank { repo.url }}：请求超时", e))
                } else {
                    throw e
                }
            } catch (e: Exception) {
                Result.failure(IllegalStateException("${repo.label.ifBlank { repo.url }}：${e.message ?: "加载失败"}", e))
            }
        }
    }

    /**
     * Install a source from a repository entry.
     * Downloads the .js file and installs via SourceController.
     */
    suspend fun installSource(entry: RepositoryEntry): DataResult<Unit> {
        if (!isRepositoryUrlValid(entry.url)) {
            return DataResult.error("Source download URL is invalid")
        }
        var targetFile: File? = null
        return try {
            val cacheDir = File(APP.getCachePath("source_v3_repo"))
            cacheDir.mkdirs()
            val downloadedFile = File.createTempFile("repository-", ".js", cacheDir)
            targetFile = downloadedFile
            withContext(Dispatchers.IO) {
                withTimeout(60_000) {
                    val response = KtorUtil.client.get(entry.url)
                    check(response.status.value in 200..299) { "HTTP ${response.status.value}" }
                    val body: ByteArray = response.body()
                    check(body.isNotEmpty()) { "Download failed" }
                    downloadedFile.writeBytes(body)
                    validateRepositoryDownload(entry, downloadedFile)
                }
            }

            val result = sourceController.appendOrUpdateSource(downloadedFile)
            result.map { Unit }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            DataResult.error("Source download timed out", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataResult.error(e.message ?: "Install failed", e)
        } finally {
            targetFile?.delete()
        }
    }
}
