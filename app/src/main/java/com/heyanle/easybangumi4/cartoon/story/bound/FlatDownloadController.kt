package com.heyanle.easybangumi4.cartoon.story.bound

import androidx.annotation.WorkerThread
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.hippo.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FlatVideoItem(
    val fileName: String,
    val uri: String,
    val size: Long,
    val lastModified: Long,
)

/**
 * 扁平下载目录扫描器：目录本身即索引，文件列表实时扫描所得，不维护独立清单。
 */
class FlatDownloadController(
    private val flatDownloadPreference: FlatDownloadPreference,
) {

    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.SINGLE)

    private val _flatVideos = MutableStateFlow<List<FlatVideoItem>>(emptyList())
    val flatVideos: StateFlow<List<FlatVideoItem>> = _flatVideos.asStateFlow()

    init {
        scope.launch {
            flatDownloadPreference.realFlatDownloadUri.collect {
                innerRefresh()
            }
        }
    }

    fun refresh() {
        scope.launch {
            innerRefresh()
        }
    }

    suspend fun awaitRefresh() {
        innerRefresh()
    }

    @WorkerThread
    private suspend fun innerRefresh() {
        val root = UniFile.fromUri(APP, flatDownloadPreference.realFlatDownloadUri.value)
        val list = root?.listFiles()?.mapNotNull { file ->
            val name = file.name ?: return@mapNotNull null
            // 与本地番源一致，暂时只支持 mp4 和 mkv
            if (!name.endsWith(".mp4") && !name.endsWith(".mkv")) {
                return@mapNotNull null
            }
            FlatVideoItem(
                fileName = name,
                uri = file.uri.toString(),
                size = file.length(),
                lastModified = file.lastModified(),
            )
        }?.sortedBy { it.fileName } ?: emptyList()
        _flatVideos.value = list
    }

}
