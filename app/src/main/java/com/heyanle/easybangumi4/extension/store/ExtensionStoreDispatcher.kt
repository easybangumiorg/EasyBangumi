package com.heyanle.easybangumi4.extension.store

import com.heyanle.easybangumi4.bus.DownloadingBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * 拓展下载分发器
 * Created by heyanlin on 2023/11/16.
 */
class ExtensionStoreDispatcher(
    private val downloadingBus: DownloadingBus,
) {

    // 同时下载的番剧源个数
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.IO.limitedParallelism(3)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // 番剧下载 Info
    // 只要 job 是 active 状态就代表正在下载，不支持断点续传
    data class ExtensionDownloadInfo(
        val jobUUID: String,
        val downloadPath: String,
        val extensionPath: String,
        val fileName: String,
        val isError: Boolean = false,
        val errorMsg: String = "",
        val throwable: Throwable? = null,
        val remoteInfo: ExtensionStoreRemoteInfo,
    )


    // 不支持断点下载，该数据不用考虑持久化
    private val _downloadInfoFlow = MutableStateFlow<Map<String, ExtensionDownloadInfo>>(emptyMap())
    val downloadInfoFlow = _downloadInfoFlow.asStateFlow()

    private val jobMap = ConcurrentHashMap<String, Job>()

    // 如果正在下载则停止，否则下载
    fun toggle(remoteInfo: ExtensionStoreRemoteInfo){

    }

    fun start(remoteInfo: ExtensionStoreRemoteInfo){

    }

    fun stop(remoteInfo: ExtensionStoreRemoteInfo){

    }

    fun getDownloadingInfo(key: String): DownloadingBus.DownloadingInfo {
        return downloadingBus.getInfo(DownloadingBus.DownloadScene.EXTENSION, key)
    }

    fun removeDownloadInfo(key: String) {
        downloadingBus.remove(DownloadingBus.DownloadScene.EXTENSION, key)
    }

    private fun innerDownloadExtension(remoteInfo: ExtensionStoreRemoteInfo): Job {
        return scope.launch {
            // 下载

            // 复制

            // 写入 json

        }
    }

    private suspend fun download(downloadUrl: String, path: String) {

    }

    private fun remove(pkg: String){
        update(pkg){
            val jobUUID = it?.jobUUID
            if(jobUUID != null){
                jobMap[jobUUID]?.cancel()
                jobMap.remove(jobUUID)
            }
            null
        }
    }

    private fun error(pkg: String, errorMsg: String, throwable: Throwable?){
        update(pkg) {
            // 没有的任务直接放弃该次 error
            it?.copy(
                isError = true,
                errorMsg = errorMsg,
                throwable = throwable,
            )
        }
    }

    private fun update(pkg: String, block: (ExtensionDownloadInfo?) -> ExtensionDownloadInfo?) {
        while (true) {
            val prevValue = _downloadInfoFlow.value
            val map = prevValue.toMutableMap()
            val old = map[pkg]
            val n = block(old)
            if (n == null) {
                map.remove(pkg)
            } else {
                map[pkg] = n
            }
            // 修改成功之后如果 job 修改则需要 cancel
            if (_downloadInfoFlow.compareAndSet(prevValue, map)) {
                if (old?.jobUUID != null && old.jobUUID != n?.jobUUID) {
                    jobMap[old.jobUUID]?.cancel()
                }
                return
            }
        }
    }

}