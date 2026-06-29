package org.easybangumi.next.shared.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD

/**
 * 本地番剧控制器
 * 负责扫描本地文件夹，索引本地番剧
 */
class LocalCartoonController(
    private val localPreference: LocalPreference,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val _items = MutableStateFlow<List<LocalCartoonItem>>(emptyList())
    val items: StateFlow<List<LocalCartoonItem>> = _items.asStateFlow()

    init {
        scope.launch {
            localPreference.realLocalFolderUFD.collectLatest {
                refresh()
            }
        }
    }

    /**
     * 刷新本地番剧列表
     */
    fun refresh() {
        scope.launch {
            mutex.withLock {
                innerRefresh()
            }
        }
    }

    private suspend fun innerRefresh() {
        val folder = localPreference.getLocalFolder()
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            _items.value = emptyList()
            return
        }

        val items = mutableListOf<LocalCartoonItem>()
        folder.listFiles()?.forEach { file ->
            if (file != null && file.isDirectory()) {
                val item = LocalItemFactory.getItemFromFolder(file)
                if (item != null) {
                    items.add(item)
                }
            }
        }

        _items.value = items
    }

    /**
     * 根据 itemId 查找本地番剧
     */
    fun findByItemId(itemId: String): LocalCartoonItem? {
        return _items.value.find { it.itemId == itemId }
    }

    /**
     * 根据 CartoonIndex 的 id 查找本地番剧
     */
    fun findByCartoonId(id: String): LocalCartoonItem? {
        return _items.value.find { it.itemId == id }
    }

    /**
     * 获取本地番剧的 UniFile
     */
    fun getItemFolder(item: LocalCartoonItem): UniFile? {
        return UniFileFactory.fromUFD(item.folderUFD)
    }

    /**
     * 删除整个本地番剧文件夹
     */
    fun deleteItem(item: LocalCartoonItem): Boolean {
        val folder = UniFileFactory.fromUFD(item.folderUFD) ?: return false
        val result = folder.delete()
        if (result) {
            refresh()
        }
        return result
    }

    /**
     * 删除单个剧集文件
     */
    fun deleteEpisode(item: LocalCartoonItem, episode: LocalEpisode): Boolean {
        val folder = UniFileFactory.fromUFD(item.folderUFD) ?: return false
        var deleted = false

        // 删除媒体文件
        val mediaFile = UniFileFactory.fromUFD(episode.mediaUFD)
        if (mediaFile != null && mediaFile.exists()) {
            deleted = mediaFile.delete()
        }

        // 删除 NFO 文件
        if (episode.nfoUFD != null) {
            val nfoFile = UniFileFactory.fromUFD(episode.nfoUFD)
            if (nfoFile != null && nfoFile.exists()) {
                nfoFile.delete()
            }
        }

        if (deleted) {
            refresh()
        }
        return deleted
    }

    /**
     * 获取已存在的剧集编号集合
     */
    fun getExistingEpisodes(itemId: String): Set<Int> {
        val item = findByItemId(itemId) ?: return emptySet()
        return item.episodes.map { it.episode }.toSet()
    }
}
