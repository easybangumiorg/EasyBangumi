package com.heyanle.easybangumi4.ui.local_play

import com.heyanle.easybangumi4.cartoon_download.entity.LocalEpisode
import com.heyanle.easybangumi4.cartoon_download.entity.LocalPlayLine
import com.heyanle.easybangumi4.source_api.entity.Episode

data class LocalPlayLineWrapper (
    val playLine: LocalPlayLine,
    val isReverse: Boolean = false,
    val filter: (LocalEpisode) -> Boolean = {true},
    val comparator: Comparator<LocalEpisode>,
){

    val sortedEpisodeList: List<LocalEpisode> by lazy {
        playLine.list.filter(filter).sortedWith { o1, o2 ->
            if(isReverse){
                - comparator.compare(o1, o2)
            }else{
                comparator.compare(o1, o2)
            }
        }
    }
}