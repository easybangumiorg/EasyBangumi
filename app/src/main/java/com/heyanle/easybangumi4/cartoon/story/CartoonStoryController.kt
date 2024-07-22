package com.heyanle.easybangumi4.cartoon.story

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalEpisode
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalMsg
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import kotlinx.coroutines.flow.StateFlow

/**
 * 番剧下载 和 本地番源一起的耦合控制器
 * Created by heyanle on 2024/7/15.
 * https://github.com/heyanLE
 */
interface CartoonStoryController {

    // 番剧下载信息，包括持久化的请求和运行时的 Runtime
    val downloadInfoList: StateFlow<DataResult<List<CartoonDownloadInfo>>>

    // 本地剧集信息包含与其关联的番剧下载信息
    val storyItemList: StateFlow<DataResult<List<CartoonStoryItem>>>


    // 下载任务新建和删除
    fun newDownloadReq(reqList: Collection<CartoonDownloadReq>)

    fun removeDownloadReq(reqList: Collection<CartoonDownloadReq>)

    // 尝试重新下载，只有下载错误或者重启后点击才能恢复
    fun tryResumeDownloadReq(info: CartoonDownloadInfo)



    // 本地剧集新建和删除，这里会同时删除关联的下载信息

    fun refreshLocal()
    suspend fun newStory(localMsg: CartoonLocalMsg): String?

    fun removeStory(cartoonStoryItem: Collection<CartoonStoryItem>)

    fun removeEpisodeItem(episode: Collection<CartoonLocalEpisode>)

}