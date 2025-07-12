package org.easybangumi.next.shared.plugin.api.component.play

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.CartoonPlayCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.plugin.api.component.Component


/**
 * Created by HeYanLe on 2024/12/8 22:05.
 * https://github.com/heyanLE
 */

interface PlayComponent: Component {

    // 搜索播放线路
    data class PlayLineSearchParam(
        // 来源番的数据
        val cartoonCover: CartoonCover,
        // 用户手动输入的搜索关键字，可能为空
        val keyword: String? = null,
        // 用户手动输入的 url，可能为空
        val webUrl: String? = null,
    )

    data class PlayLineSearchResultItem(
        val fromCartoonCover: CartoonPlayCover,
        val playLineList: List<PlayerLine>,
    )

    // 搜索播放线路
    suspend fun searchPlayLines(
        param: PlayLineSearchParam,
    ): DataState<List<PlayLineSearchResultItem>>


    suspend fun getPlayInfo(
        cartoonPlayCover: CartoonPlayCover,
        playerLine: PlayerLine,
        episode: Episode,
    ): DataState<PlayInfo>

}
