package com.heyanle.easybangumi4.extension.store

import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.bus.DownloadingBus
import com.heyanle.easybangumi4.utils.OkhttpHelper
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.CancellationException
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

    private suspend fun CoroutineScope.download(
        remoteInfo: ExtensionStoreRemoteInfoItem,
        filePath: String,
    ) {
        withContext(dispatcher){
            try {
                val tempFile = File("${filePath}.temp")
                val file = File(filePath)

                tempFile.parent?.let {
                    File(it).mkdirs()
                }

                file.delete()
                tempFile.delete()
                tempFile.createNewFile()
                OkhttpHelper.client.newCall(Request.Builder().url(remoteInfo.fileUrl).get().build())
                    .execute().use {  resp ->
                        val body = resp.body
                        if(!resp.isSuccessful || body == null){
                            error(remoteInfo.pkg, resp.message?:"")
                        }else{
                            body.use {  b ->
                                b.byteStream().use { stream ->
                                    tempFile.outputStream().use { o ->

                                        val fileSize = (resp.header("Content-Length", "-1")?:"-1").toLongOrNull() ?: -1L
                                        val info = getInfo(remoteInfo)
                                        info.status.value = stringRes(com.heyanle.easy_i18n.R.string.downloading)

                                        val byteArray = ByteArray(1024*10)
                                        var length = 0
                                        var total = 0
                                        do {
                                            info.process.value = if(fileSize >= total) total/fileSize.toFloat() else -1f
                                            length = stream.read(byteArray)
                                            total += length
                                            o.write(byteArray, 0, length)
                                            o.flush()
                                        } while (length != -1 && isActive)
                                    }
                                }
                            }
                            tempFile.renameTo(file)
                        }
                    }
            }catch (e: IOException){
                e.printStackTrace()
                error(remoteInfo.pkg, e.message?:"", e)
            }

        }


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

    private fun error(pkg: String, errorMsg: String, throwable: Throwable? = null){
        update(pkg) {
            // 没有的任务直接放弃该次 error
            it?.copy(
                isError = true,
                errorMsg = errorMsg,
                throwable = throwable,
            )
        }
    }

    private fun getInfo(remoteInfo: ExtensionStoreRemoteInfoItem): DownloadingBus.DownloadingInfo{
        return downloadingBus.getInfo(DownloadingBus.DownloadScene.EXTENSION, remoteInfo.pkg)
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