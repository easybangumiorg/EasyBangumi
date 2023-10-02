package com.heyanle.easybangumi4.download.entity

import com.arialyy.aria.core.download.M3U8Entity
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo

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
    val cartoonTitle: String,
    val cartoonCover: String,
    val cartoonDescription: String,
    val cartoonGenre: String,

    val playLine: PlayLine,

    val episodeLabel: String,
    val episodeIndex: Int,


    val state: Int, // -1 -> error, 0 -> doing, 1 -> waiting（当前步骤完成等待调度） 2 -> Completely
    val currentSteps: Int, // 当前步骤

    val stepsChain: List<String>, // 该任务需要的所有步骤

    val bundle: DownloadBundle = DownloadBundle(),
    val errorMsg: String = "",

    val filePathWithoutSuffix: String, // 下载目标路径+文件名不含后缀
) {

    fun needDispatcher() = state == 1
}

/**
 * 下载所有环节需要暂存的数据
 */
data class DownloadBundle(
    // 解析后的最终下载地址和解码方式
    var playerInfo: PlayerInfo? = null,
    // aria 任务
    var ariaId: Long = -1,
    // aria 的 m3u8 实体（如果是流媒体的解码形式）
    // 含 m3u8 最终下载路径
    var m3U8Entity: M3U8Entity? = null,

    var realFilePath: String = "", // 最终下载的路径+文件名含后缀
)