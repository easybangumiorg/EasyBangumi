package com.heyanle.easybangumi4.plugin.extension.remote

import com.heyanle.easybangumi4.base.DataResult
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.map

/**
 * Created by heyanlin on 2025/8/14.
 */
class ExtensionRemoteController(
    jsonFileProvider: JsonFileProvider,
    private val cachePath: String,
) {

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val repository = jsonFileProvider.extensionRepository

    data class RemoteInfoState(
        val loading: Boolean = true,
        // repository: remoteInfo
        val remoteInfo: Map<String, RemoteInfo> = emptyMap()
    )

    private val _remote = MutableStateFlow(RemoteInfoState())
    val remote = _remote.asStateFlow()


    val repositoryState = repository.flow.map {
        it.okOrNull()?:emptyList()
    }.stateIn(scope, SharingStarted.Lazily, emptyList())

    private val remoteInfoTemp = ConcurrentHashMap<String, List<RemoteInfo>>()

    fun refresh() {
        remoteInfoTemp.clear()
        scope.launch {
            load(repositoryState.value)
        }
    }

    init {
        scope.launch {
            repositoryState.collectLatest {
                load(it)
            }
        }
    }

    fun updateRepository(list: List<String>) {
        repository.set(list)
    }

    private suspend fun load(repository: List<String>) {
        val map = hashMapOf<String, RemoteInfo>()
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

    private suspend fun getRemote(repository: String): Deferred<List<RemoteInfo>> {
        return scope.async {
            val temp = remoteInfoTemp[repository]
            if (temp != null) {
                return@async temp
            }
            val jsonlFile = File(cachePath, "remote_extension/${System.currentTimeMillis()}.jsonl")
            repository.downloadTo(jsonlFile.absolutePath)
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