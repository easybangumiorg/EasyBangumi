package com.heyanle.easybangumi4.plugin.extension.remote

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.case.ExtensionCase
import com.heyanle.easybangumi4.plugin.extension.ExtensionControllerV2
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProvider
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProviderV2
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionCryLoader
import com.heyanle.easybangumi4.utils.downloadTo
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanlin on 2025/8/14.
 */
class ExtensionRepoController(
    private val extensionCase: ExtensionCase,
    private val extensionControllerV2: ExtensionControllerV2,
    private val remoteController: ExtensionRemoteController,
    private val cache: String
) {


    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)


    data class State(
        val loading: Boolean = true,
        val remoteLoading: Boolean = true,
        val remoteLocalInfo: Map<String, ExtensionRemoteLocalInfo> = emptyMap(),
    )
    private val _state = MutableStateFlow<State> (State())
    val state = _state.asStateFlow()

    init {
        scope.launch {
            combine(
                extensionCase.flowExtensionState(),
                remoteController.remote
            ) { local, remote ->
                if (local.loading) {
                    _state.update {
                        it.copy(
                            loading = true,
                            remoteLoading = remote.loading,
                        )
                    }
                } else {
                    val map = mutableMapOf<String, ExtensionRemoteLocalInfo>()
                    val keyList = local.extensionInfoMap.keys + remote.remoteInfo.keys
                    keyList.forEach {
                        val remote = remote.remoteInfo[it]
                        val local = local.extensionInfoMap[it]
                        val remoteLocalInfo = ExtensionRemoteLocalInfo(
                            remoteInfo = remote,
                            localInfo = local
                        )
                        map[it] = remoteLocalInfo
                    }
                    _state.update {
                        it.copy(
                            loading = false,
                            remoteLocalInfo = map,
                            remoteLoading = remote.loading,
                        )
                    }

                }
            }.collect()
        }
    }

    suspend fun appendOrUpdate(
        remoteInfo: RemoteInfo
    ): DataResult<Unit> {
        return scope.async {
            val url = remoteInfo.url
            val file = File(cache, remoteInfo.key)
            url.downloadTo(file.absolutePath)
            if (!file.exists()) {
                return@async DataResult.Error<Unit>(
                    "下载失败",
                    throwable = IOException("downloadError")
                )
            }
            val buffer = ByteArray(JSExtensionCryLoader.FIRST_LINE_MARK.size)
            val size = file.inputStream().use {
                it.read(buffer)
            }
            val isCry = size == JSExtensionCryLoader.FIRST_LINE_MARK.size && buffer.contentEquals(JSExtensionCryLoader.FIRST_LINE_MARK)
            val targetFile = if (isCry) {
                File(cache, remoteInfo.key + ".${JsExtensionProviderV2.EXTENSION_CRY_SUFFIX}")
            } else {
                File(cache, remoteInfo.key + ".${JsExtensionProviderV2.EXTENSION_SUFFIX}")
            }
            file.renameTo(targetFile)
            return@async extensionControllerV2.appendOrUpdateExtension(targetFile).map { Unit }
        }.await()


    }

    suspend fun delete(key: String): DataResult<Unit> {
        return extensionControllerV2.deleteExtension(key)
    }

}