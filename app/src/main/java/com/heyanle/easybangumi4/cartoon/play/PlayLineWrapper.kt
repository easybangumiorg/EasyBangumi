package com.heyanle.easybangumi4.cartoon.play

import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine

/**
 * 组合 playLine，添加过滤和排序功能
 * Created by heyanlin on 2023/11/3.
 */
class PlayLineWrapper(
    val playLine: PlayLine,
    val filter: (Episode) -> Boolean,
    val comparator: Comparator<Episode>,
){

    val sortedEpisodeList: List<Episode> by lazy {
        playLine.episode.filter(filter).sortedWith(comparator)
    }


}