package com.heyanle.easybangumi4.download.entity

import com.arialyy.aria.core.download.M3U8Entity
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo

/**
 * Created by HeYanLe on 2023/9/17 15:40.
 * https://github.com/heyanLE
 */
data class DownloadItem(
    val uuid: String,
    // cartoon 外键
    val cartoonId: String,
    val cartoonUrl: String,
    val cartoonSource: String,

    // 展示数据
    val sourceLabel: String,
    val cartoonTitle: String,
    val cartoonCover: String,
    val cartoonDescription: String,
    val cartoonGenre: String,

    val playLine: PlayLine,
    val episode: Episode,


    val state: Int, // -1 -> error, 0 -> waiting ,1 -> doing, 2 -> step completely（当前步骤完成等待调度） 3 -> Completely
    val currentSteps: Int, // 当前步骤

    val stepsChain: List<String>, // 该任务需要的所有步骤

    val bundle: DownloadBundle = DownloadBundle(),
    val errorMsg: String = "",

    val folder: String, // 目标路径
    val fileNameWithoutSuffix: String, // 目标文件名

    val isRemoved: Boolean = false, // 是否删除，除了 ariaStep 其他步骤需要等该步骤处理完才能清理
) {

    fun needDispatcher() = state == 0 || state == 2 || state == -1 || isRemoved
}

/**
 * 下载所有环节需要暂存的数据
 */
data class DownloadBundle(
    // 解析后的最终下载地址和解码方式
    var playerInfo: PlayerInfo? = null,
    // aria 任务
    var ariaId: Long = -1,
    var downloadFolder: String = "",
    var downloadFileName: String = "",
    // aria 的 m3u8 实体（如果是流媒体的解码形式）
    // 含 m3u8 最终下载路径
    var m3U8Entity: M3U8Entity? = null,

    var filePathBeforeCopy: String = "", // 复制前路径

    var needRefreshMedia: Boolean = false, // 是否需要下载完刷新媒体
)