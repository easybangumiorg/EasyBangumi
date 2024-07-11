package com.heyanle.easybangumi4.cartoon_local

import android.net.Uri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon_local.entity.CartoonLocalItem
import com.heyanle.easybangumi4.cartoon_local.entity.LocalItemFactory
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.stringRes
import com.hippo.unifile.UniFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.withLock
import java.util.concurrent.locks.ReentrantLock

/**
 * 本地番源
 * Created by heyanlin on 2024/7/4.
 */
class CartoonLocalController(
    private val localCartoonPreference: LocalCartoonPreference,
) {

    data class State(
        val loading: Boolean = true,
        val errorMsg: String? = null,
        val error: Throwable? = null,

        val localCartoonItem: Map<String, CartoonLocalItem> = mapOf()
    )

    private val localEpisodeLock = ReentrantLock()
    private val localEpisodeMap = HashMap<String, Set<Int>>()

    private val _flowState = MutableStateFlow(State())
    val flowState = _flowState.asStateFlow()

    private val loadSingleDispatcher = CoroutineProvider.CUSTOM_SINGLE
    private val scope = MainScope()

    private var lastLoadJob: Job? = null

    init {
        scope.launch {
            localCartoonPreference.realLocalUri.collectLatest {
                refresh(it)
            }
        }

    }

    fun refresh(
        uri: Uri? = null
    ) {
        lastLoadJob?.cancel()
        lastLoadJob = scope.launch(loadSingleDispatcher) {
            innerRefresh(uri)
        }
    }

    suspend fun innerRefresh(
        uri: Uri? = null
    ) {
        _flowState.update {
            it.copy(loading = true)
        }
        try {
            val ru = uri ?: localCartoonPreference.realLocalUri.value
            val uniFile = UniFile.fromUri(APP, ru)
            if (uniFile == null) {
                _flowState.update {
                    it.copy(
                        loading = false,
                        errorMsg = "无法打开文件夹"
                    )
                }
                stringRes(com.heyanle.easy_i18n.R.string.local_folder_error).moeSnackBar()
                return
            }
            val items = uniFile.listFiles()?.mapNotNull {
                LocalItemFactory.getItemFromFolder(it)
            } ?: emptyList()
            localEpisodeLock.withLock {
                localEpisodeMap.clear()
                items.forEach { item ->
                    localEpisodeMap[item.itemId] = item.episodes.map { it.episode }.toSet()
                }
            }
            _flowState.update {
                it.copy(
                    loading = false,
                    errorMsg = null,
                    error = null,
                    localCartoonItem = items.associateBy { it.folderUri }
                )
            }
        }catch (e: Throwable){
            _flowState.update {
                it.copy(
                    loading = false,
                    errorMsg = e.message,
                    error = e
                )
            }
            e.printStackTrace()
        }



    }

    fun newLocal(cartoonInfo: CartoonInfo, label: String) {
        scope.launch(Dispatchers.IO) {
            lastLoadJob?.join()
            if (flowState.value.localCartoonItem.any { it.key == label }) {
                return@launch
            }
            val rootFolder = localCartoonPreference.realLocalUri.value.let {
                UniFile.fromUri(APP, it)
            } ?: return@launch
            val realFolder = LocalItemFactory.newItemFolder(cartoonInfo, label, rootFolder) ?: return@launch
            val item = LocalItemFactory.getItemFromFolder(realFolder) ?: return@launch
            _flowState.update {
                it.copy(
                    localCartoonItem = it.localCartoonItem + (item.itemId to item)
                )
            }

        }
    }

    fun putLocalEpisode(itemId: String, episodes: Set<Int>){
        localEpisodeLock.withLock {
            val set = localEpisodeMap[itemId]?.toMutableSet() ?: mutableSetOf()
            set.addAll(episodes)
            localEpisodeMap[itemId] = set
        }
    }

    fun getLocalEpisodes(itemId: String): Set<Int>{
        return localEpisodeLock.withLock {
            localEpisodeMap[itemId] ?: emptySet()
        }
    }

    fun checkEpisodeExist(itemId: String, episode: Int): Boolean{
        return localEpisodeLock.withLock {
            localEpisodeMap[itemId]?.contains(episode) == true
        }
    }


}