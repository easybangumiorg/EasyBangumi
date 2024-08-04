package com.heyanle.easybangumi4.cartoon.story

import androidx.core.net.toUri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalEpisode
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalMsg
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.story.download.req.CartoonDownloadReqController
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.cartoon.story.local.CartoonLocalController
import com.hippo.unifile.UniFile
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by heyanle on 2024/7/15.
 * https://github.com/heyanLE
 */
class CartoonStoryControllerImpl(
    private val cartoonDownloadDispatcher: CartoonDownloadDispatcher,
    private val cartoonDownloadReqController: CartoonDownloadReqController,
    private val cartoonLocalController: CartoonLocalController,
) : CartoonStoryController {

    private val scope = MainScope()

    private val _downloadInfo =
        MutableStateFlow<DataResult<List<CartoonDownloadInfo>>>(DataResult.Loading())
    override val downloadInfoList: StateFlow<DataResult<List<CartoonDownloadInfo>>> =
        _downloadInfo.asStateFlow()

    private val _storyItemList =
        MutableStateFlow<DataResult<List<CartoonStoryItem>>>(DataResult.Loading())
    override val storyItemList: StateFlow<DataResult<List<CartoonStoryItem>>> =
        _storyItemList.asStateFlow()

    init {
        scope.launch {
            combine(
                cartoonDownloadDispatcher.runtimeMap,
                cartoonDownloadReqController.downloadItem,
            ) { runtimeMap, reqList ->

                _downloadInfo.update {
                    reqList.map {
                        it.map {
                            CartoonDownloadInfo(
                                it,
                                runtimeMap[it.uuid]
                            )
                        }
                    }
                }
            }.collect()
        }

        scope.launch {
            combine(
                _downloadInfo,
                cartoonLocalController.flowState
            ) { download, local ->
                _storyItemList.update {
                    local.map {
                        it.map {
                            CartoonStoryItem(
                                it.value,
                                download.okOrNull()
                                    ?.filter { d -> d.req.toLocalItemId == it.value.itemId }
                                    ?: emptyList()
                            )
                        }
                    }
                }
            }.collect()

        }
    }

    override fun newDownloadReq(reqList: Collection<CartoonDownloadReq>) {
        cartoonDownloadReqController.newDownloadItem(reqList)
        cartoonDownloadDispatcher.newRequest(reqList)
    }

    override fun removeDownloadReq(reqList: Collection<CartoonDownloadReq>) {
        cartoonDownloadReqController.removeDownloadItem(reqList.map { it.uuid })
        cartoonDownloadDispatcher.remove(reqList)
    }

    override fun tryResumeDownloadReq(info: CartoonDownloadInfo, closeQuickMode: Boolean) {
        if (!closeQuickMode || !info.req.quickMode) {
            cartoonDownloadDispatcher.tryResume(listOf(info.req))

        } else {
            cartoonDownloadDispatcher.remove(listOf(info.req))
            cartoonDownloadReqController.removeDownloadItem(info.req.uuid)
            cartoonDownloadReqController.newDownloadItem(
                listOf(
                    info.req.copy(
                        quickMode = false
                    )
                )
            )
        }
    }

    override suspend fun newStory(localMsg: CartoonLocalMsg): String? {
        return suspendCoroutine<String?> { con ->
            cartoonLocalController.newLocal(localMsg) {
                con.resume(it)
            }
        }

    }

    override fun refreshLocal() {
        cartoonLocalController.refresh()
    }

    override fun removeStory(cartoonStoryItem: Collection<CartoonStoryItem>) {
        cartoonStoryItem.forEach {
            val file = UniFile.fromUri(APP, it.cartoonLocalItem.folderUri.toUri())
            file?.delete()
        }

        cartoonLocalController.refresh()
        cartoonDownloadReqController.removeDownloadItemWithItemId(cartoonStoryItem.map { it.cartoonLocalItem.itemId })
        cartoonDownloadDispatcher.removeWithItemId(cartoonStoryItem.map { it.cartoonLocalItem.itemId })
    }

    override fun removeEpisodeItem(episode: Collection<CartoonLocalEpisode>) {
        episode.forEach {
            UniFile.fromUri(APP, it.nfoUri.toUri())?.delete()
            UniFile.fromUri(APP, it.mediaUri.toUri())?.delete()
        }
        cartoonLocalController.refresh()
    }
}