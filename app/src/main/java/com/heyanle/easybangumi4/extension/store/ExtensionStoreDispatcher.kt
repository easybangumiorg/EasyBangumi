package com.heyanle.easybangumi4.extension.store

import com.heyanle.easybangumi4.bus.DownloadingBus
import com.heyanle.easybangumi4.utils.OkhttpHelper
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * 拓展下载分发器
 * Created by heyanlin on 2023/11/16.
 */
class ExtensionStoreDispatcher(
    private val cacheFolder: String, // 临时下载路径
    private val storeFolder: String, // 拓展市场根路径
    private val extensionFolder: String, // 番源下载路径
    private val downloadingBus: DownloadingBus,
) {

    // 单线程，保证各种数据处理是串行的
    @OptIn(ExperimentalCoroutinesApi::class)
    private val singleDispatcher = Dispatchers.IO.limitedParallelism(1)
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
        val remoteInfo: ExtensionStoreRemoteInfoItem,
    )


    // 不支持断点下载，该数据不用考虑持久化
    private val _downloadInfoFlow = MutableStateFlow<Map<String, ExtensionDownloadInfo>>(emptyMap())
    val downloadInfoFlow = _downloadInfoFlow.asStateFlow()

    private val jobMap = ConcurrentHashMap<String, Job>()

    // 下载成功清单
    private val _installedExtension = MutableStateFlow<Map<String, OfficialExtensionItem>?>(null)
    val installedExtension = _installedExtension.asStateFlow()

    private var lastSaveJob: Job? = null

    private val extensionItemJson = File(storeFolder, "official.json")
    private val extensionItemJsonTemp = File(storeFolder, "official.json.bk")

    // 如果正在下载则停止，否则下载
    fun toggle(remoteInfo: ExtensionStoreRemoteInfoItem) {
        scope.launch(singleDispatcher) {
            val currentMap = downloadInfoFlow.first()
            val current = currentMap[remoteInfo.pkg]
            if(current != null && jobMap[current.jobUUID]?.isActive == true){
                remove(remoteInfo.pkg)
            }else{
                innerDownloadExtension(remoteInfo)
            }
        }
    }

    fun start(remoteInfo: ExtensionStoreRemoteInfoItem) {
        scope.launch(singleDispatcher) {
            innerDownloadExtension(remoteInfo)
        }
    }

    fun stop(remoteInfo: ExtensionStoreRemoteInfoItem) {
        scope.launch(singleDispatcher) {
            remove(remoteInfo.pkg)
        }
    }

    fun getJob(key: String): Job? {
        return jobMap[key]
    }

    fun getDownloadingInfo(key: String): DownloadingBus.DownloadingInfo {
        return downloadingBus.getInfo(DownloadingBus.DownloadScene.EXTENSION, key)
    }

    fun removeDownloadInfo(key: String) {
        downloadingBus.remove(DownloadingBus.DownloadScene.EXTENSION, key)
    }

    init {
        scope.launch (singleDispatcher) {
            _installedExtension.collectLatest {
                if(it != null){
                    saveJson(it)
                }
            }
        }
        scope.launch (singleDispatcher) {
            if(!extensionItemJson.exists() && extensionItemJsonTemp.exists()){
                extensionItemJsonTemp.renameTo(extensionItemJson)
                extensionItemJsonTemp.delete()
            }
            if(extensionItemJson.exists()){
                extensionItemJson.readText().jsonTo<Map<String, OfficialExtensionItem>>()?.let { json ->
                    _installedExtension.update {
                        json
                    }
                }
            }
            _installedExtension.update {
                it ?: emptyMap()
            }
        }

    }

    private fun innerDownloadExtension(remoteInfo: ExtensionStoreRemoteInfoItem) {
        val downloadInfo = ExtensionDownloadInfo(
            jobUUID = remoteInfo.pkg + remoteInfo.md5 + System.currentTimeMillis(),
            downloadPath = cacheFolder,
            extensionPath = extensionFolder,
            fileName = remoteInfo.getInstalledFileName(),
            isError = false,
            errorMsg = "",
            throwable = null,
            remoteInfo = remoteInfo
        )
        val job = scope.launch {
            // 下载
            if (!download(downloadInfo)) {
                return@launch
            }

            // 复制
            try{
                val source = File(downloadInfo.downloadPath, downloadInfo.fileName)
                val targetTemp = File(downloadInfo.extensionPath, downloadInfo.fileName + ".temp")
                val target = File(downloadInfo.extensionPath, downloadInfo.fileName)
                if(!source.exists()){
                    error(downloadInfo.remoteInfo.pkg,  stringRes(com.heyanle.easy_i18n.R.string.download_error))
                    return@launch
                }
                targetTemp.delete()
                target.delete()
                source.copyTo(targetTemp)
                targetTemp.renameTo(target)
            }catch (e: IOException){
                error(downloadInfo.remoteInfo.pkg, e.message ?: "", e)
                return@launch
            }

            // 写入 json
            _installedExtension.update {
                val map = it?.toMutableMap() ?: mutableMapOf()
                map[remoteInfo.pkg] = OfficialExtensionItem(remoteInfo, File(downloadInfo.extensionPath, downloadInfo.fileName).absolutePath)
                map
            }

            // 删除任务
            remove(remoteInfo.pkg)


        }
        update(remoteInfo.pkg){
            downloadInfo
        }
        jobMap[downloadInfo.jobUUID] = job
    }

    private suspend fun download(
        downInfo: ExtensionDownloadInfo
    ): Boolean {
        return withContext(dispatcher) {
            try {

                val parent = File(downInfo.downloadPath)
                val tempFile = File(parent, "${downInfo.fileName}.temp")
                val file = File(parent, downInfo.fileName)

                parent.mkdirs()

                file.delete()
                tempFile.delete()
                tempFile.createNewFile()
                OkhttpHelper.client.newCall(
                    Request.Builder().url(downInfo.remoteInfo.fileUrl).get().build()
                )
                    .execute().use { resp ->
                        val body = resp.body
                        if (!resp.isSuccessful || body == null) {
                            error(downInfo.remoteInfo.pkg, resp.message ?: "")
                            return@withContext false
                        } else {
                            body.use { b ->

                                b.byteStream().use { stream ->
                                    tempFile.outputStream().use { o ->
                                        val fileSize =
                                            if (downInfo.remoteInfo.fileSize > 0) downInfo.remoteInfo.fileSize else (resp.header(
                                                "Content-Length",
                                                "-1"
                                            )
                                                ?: "-1").toLongOrNull() ?: -1L
                                        val info = getInfo(downInfo.remoteInfo)
                                        info.status.value =
                                            stringRes(com.heyanle.easy_i18n.R.string.downloading)

                                        val byteArray = ByteArray(1024 * 10)
                                        var length = stream.read(byteArray)
                                        var total = length
                                        while (length != -1 && isActive){
                                            info.process.value =
                                                if (fileSize >= total) total / fileSize.toFloat() else -1f
                                            o.write(byteArray, 0, length)
                                            o.flush()
                                            length = stream.read(byteArray)
                                            total += length

                                        }
                                    }
                                }
                            }
                            tempFile.renameTo(file)
                        }
                        return@withContext true
                    }
            } catch (e: IOException) {
                e.printStackTrace()
                error(downInfo.remoteInfo.pkg, e.message ?: "", e)
                return@withContext false
            }

        }



    }

    private fun remove(pkg: String) {
        update(pkg) {
            val jobUUID = it?.jobUUID
            if (jobUUID != null) {
                jobMap[jobUUID]?.cancel()
                jobMap.remove(jobUUID)
            }
            null
        }
    }

    private fun error(pkg: String, errorMsg: String, throwable: Throwable? = null) {
        update(pkg) {
            // 没有的任务直接放弃该次 error
            it?.copy(
                isError = true,
                errorMsg = errorMsg,
                throwable = throwable,
            )
        }
    }

    private fun getInfo(remoteInfo: ExtensionStoreRemoteInfoItem): DownloadingBus.DownloadingInfo {
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

    private fun saveJson(map: Map<String, OfficialExtensionItem>) {
        lastSaveJob?.cancel()
        lastSaveJob = scope.launch(singleDispatcher) {
            extensionItemJsonTemp.delete()
            extensionItemJsonTemp.createNewFile()
            yield()
            extensionItemJsonTemp.writeText(map.toJson())
            yield()
            extensionItemJson.delete()
            extensionItemJsonTemp.renameTo(extensionItemJson)
            yield()
        }
    }

}