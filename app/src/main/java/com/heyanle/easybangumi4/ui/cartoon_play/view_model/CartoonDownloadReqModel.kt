package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.source_api.entity.Episode

/**
 * B 请选择本地番源中的目标剧集 -> C
 * C 请设定各视频对应刮削数据 设定各个视频对应的目标剧集中的目标集数和标题，这里目标集数不允许重复（全局加锁统一控制）
 * Created by heyanle on 2024/7/8.
 * https://github.com/heyanLE
 */
class CartoonDownloadReqModel(
    private val cartoonInfo: CartoonInfo,
    private val playerLineWrapper: PlayLineWrapper,
    private val episodes: List<Episode>,
) {




}