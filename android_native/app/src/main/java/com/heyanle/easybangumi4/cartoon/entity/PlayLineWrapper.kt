package com.heyanle.easybangumi4.cartoon.entity

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.ui.common.proc.SortBy
import com.heyanle.easybangumi4.utils.stringRes

/**
 * 组合 playLine，添加过滤和排序功能
 * Created by heyanlin on 2023/11/3.
 */
class PlayLineWrapper(
    val playLine: PlayLine,
    val isReverse: Boolean = false,
    val filter: (Episode) -> Boolean = {true},
    val comparator: Comparator<Episode>,
){

    companion object {
        const val SORT_DEFAULT_KEY = "default"

        val sortByDefault: SortBy<Episode> = SortBy<Episode>(
            SORT_DEFAULT_KEY,
            stringRes(R.string.default_word)
        ) { o1, o2 ->
            o1.order - o2.order
        }

        val sortByLabel: SortBy<Episode> = SortBy<Episode>(
            "label",
            stringRes(R.string.name_word)
        ) { o1, o2 ->
            o1.label.compareTo(o2.label)
        }

        val sortList = listOf(sortByDefault, sortByLabel)


        fun fromKey(playLine: PlayLine, key: String, isReverse: Boolean): PlayLineWrapper {
            val sort = sortList.find { it.id == key }?: sortByDefault
            return PlayLineWrapper(
                playLine, isReverse, {true}, sort.comparator
            )
        }
    }






    val sortedEpisodeList: List<Episode> by lazy {
        playLine.episode.filter(filter).sortedWith { o1, o2 ->
            if(isReverse){
                - comparator.compare(o1, o2)
            }else{
                comparator.compare(o1, o2)
            }
        }
    }


}