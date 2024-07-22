package com.heyanle.easybangumi4.cartoon.story.local

import android.net.Uri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalItem
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalMsg
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

    private val localEpisodeLock = ReentrantLock()
    private val localEpisodeMap = HashMap<String, Set<Int>>()

    private val _flowState = MutableStateFlow<DataResult<Map<String, CartoonLocalItem>>>(DataResult.Loading())
    val flowState = _flowState.asStateFlow()

    private val singleDispatcher = CoroutineProvider.SINGLE
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
        lastLoadJob = scope.launch(singleDispatcher) {
            innerRefresh(uri)
        }
    }

    suspend fun innerRefresh(
        uri: Uri? = null
    ) {
        _flowState.update {
           DataResult.Loading()
        }
        try {
            val ru = uri ?: localCartoonPreference.realLocalUri.value
            val uniFile = UniFile.fromUri(APP, ru)
            if (uniFile == null) {
                _flowState.update {
                    DataResult.error("无法打开文件夹")
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
                DataResult.ok(items.associateBy { it.folderUri })
            }
        }catch (e: Throwable){
            _flowState.update {
                DataResult.error(e.message?:"未知错误", e)
            }
            e.printStackTrace()
        }



    }

    fun newLocal(cartoonLocalMsg: CartoonLocalMsg, callback: (String?) -> Unit = {}){
        scope.launch(Dispatchers.IO) {
            lastLoadJob?.join()
            if (flowState.value.okOrNull()?.any { it.key == cartoonLocalMsg.itemId } == true) {
                callback(null)
                return@launch
            }
            val rootFolder = localCartoonPreference.realLocalUri.value.let {
                UniFile.fromUri(APP, it)
            }
            if (rootFolder == null) {
                callback(null)
                return@launch
            }
            val realFolder = LocalItemFactory.newItemFolder(cartoonLocalMsg, rootFolder)
            if (realFolder == null) {
                callback(null)
                return@launch
            }
            val item = LocalItemFactory.getItemFromFolder(realFolder)
            if (item == null) {
                callback(null)
                return@launch
            }
            refresh()
            callback(item.itemId)
        }
    }



}