package org.easybangumi.next.shared.download

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.buffer
import okio.use
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.PathProvider
import org.easybangumi.next.shared.download.model.DownloadReq

/**
 * 下载请求持久化控制器
 */
class DownloadReqController(
    private val pathProvider: PathProvider,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    private val _requests = MutableStateFlow<List<DownloadReq>>(emptyList())
    val requests: StateFlow<List<DownloadReq>> = _requests.asStateFlow()

    private fun getStorageDir(): UniFile? {
        val ufd = pathProvider.getFilePath("download")
        val root = UniFileFactory.fromUFD(ufd) ?: return null
        val dir = root.child("requests")
            ?: root.createDirectory("requests")?.let { root.child("requests") }
        return dir
    }

    init {
        scope.launch {
            loadAll()
        }
    }

    suspend fun save(req: DownloadReq) {
        mutex.withLock {
            val dir = getStorageDir() ?: return@withLock
            val file = dir.child("${req.uuid}.json") ?: return@withLock
            file.openSink(false).buffer().use { sink ->
                sink.writeUtf8(json.encodeToString(req))
            }
            _requests.value = _requests.value + req
        }
    }

    suspend fun remove(uuid: String) {
        mutex.withLock {
            val dir = getStorageDir() ?: return@withLock
            dir.child("$uuid.json")?.delete()
            _requests.value = _requests.value.filter { it.uuid != uuid }
        }
    }

    suspend fun removeAll(uuids: List<String>) {
        mutex.withLock {
            val dir = getStorageDir() ?: return@withLock
            val uuidSet = uuids.toSet()
            for (uuid in uuids) {
                dir.child("$uuid.json")?.delete()
            }
            _requests.value = _requests.value.filter { it.uuid !in uuidSet }
        }
    }

    suspend fun removeByLocalItemId(itemId: String) {
        mutex.withLock {
            val dir = getStorageDir() ?: return@withLock
            val toRemove = _requests.value.filter { it.toLocalItemId == itemId }
            for (req in toRemove) {
                dir.child("${req.uuid}.json")?.delete()
            }
            _requests.value = _requests.value.filter { it.toLocalItemId != itemId }
        }
    }

    fun getAll(): List<DownloadReq> {
        return _requests.value
    }

    private fun loadAll() {
        val dir = getStorageDir() ?: return
        val files = dir.listFiles() ?: return
        val requests = files
            .filter { it != null && it.getName().endsWith(".json") }
            .mapNotNull { file ->
                try {
                    val content = file!!.openSource().buffer().use { it.readUtf8() }
                    json.decodeFromString<DownloadReq>(content)
                } catch (e: Exception) {
                    null
                }
            }
        _requests.value = requests
    }
}
