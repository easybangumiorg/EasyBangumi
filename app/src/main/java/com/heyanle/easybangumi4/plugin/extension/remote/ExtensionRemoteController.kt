package com.heyanle.easybangumi4.plugin.extension.remote

import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.utils.downloadTo
import com.heyanle.easybangumi4.utils.jsonTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.map
import kotlin.collections.plus

/**
 * Created by heyanlin on 2025/8/14.
 */
class ExtensionRemoteController(
    jsonFileProvider: JsonFileProvider,
    private val cachePath: String,
) {

    companion object {
        val officeReposotory = Repository(
            "https://easybangumi.org/repository/v2/index.jsonl",
            -1
        )
    }

    data class Repository(
        val url: String,
        val order: Int,
    )
    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val repositoryHelper = jsonFileProvider.extensionRepository

    data class RemoteInfoState(
        val loading: Boolean = true,
        // repository: remoteInfo
        val remoteInfo: Map<String, RemoteInfo> = emptyMap()
    )

    private val _remote = MutableStateFlow(RemoteInfoState())
    val remote = _remote.asStateFlow()

    val repositoryState = repositoryHelper.flow

    private val remoteInfoTemp = ConcurrentHashMap<String, List<RemoteInfo>>()

    fun refresh() {
        remoteInfoTemp.clear()
        scope.launch {
            load((repositoryState.value.okOrNull()?:emptyList()) + officeReposotory)
        }
    }

    init {
        scope.launch {
            repositoryState.collectLatest {
                load((it.okOrNull()?:emptyList()) + officeReposotory)
            }
        }
    }

    fun updateRepository(list: List<Repository>) {
        repositoryHelper.set(list)

    }

    fun addRepository(url: String) {
        repositoryHelper.update {
            it + listOf(Repository(url, it.size))
        }
    }

    fun deleteRepository(repository: Repository) {
        repositoryHelper.update {
            it.filterNot { it.url == repository.url }
        }
    }

    private suspend fun load(repository: List<Repository>) {
        val map = hashMapOf<String, RemoteInfo>()
        _remote.update {
            it.copy(loading = true)
        }
        val res = repository.map {
            getRemote(it)
        }.awaitAll()
        res.reversed().forEach {
            it.forEach { remoteInfo ->
                map[remoteInfo.key] = remoteInfo
            }
        }
        _remote.update {
            RemoteInfoState(
                loading = false,
                remoteInfo = map
            )
        }
    }

    private suspend fun getRemote(repository: Repository): Deferred<List<RemoteInfo>> {
        return scope.async {
            val temp = remoteInfoTemp[repository.url]
            if (temp != null) {
                return@async temp
            }
            val jsonlFile = File(cachePath, "remote_extension/${System.currentTimeMillis()}.jsonl")
            repository.url.downloadTo(jsonlFile.absolutePath)
            jsonlFile.deleteOnExit()
            if (jsonlFile.exists()) {
                val res = arrayListOf<RemoteInfo>()
                jsonlFile.bufferedReader().use {
                    var line = it.readLine()
                    while (!line.isNullOrEmpty()) {
                        val remote = line.jsonTo<RemoteInfo>()
                        if (remote != null) {
                            res.add(remote)
                        }
                        line = it.readLine()
                    }
                }
                return@async res
            } else {
                return@async emptyList()
            }
        }
    }





}