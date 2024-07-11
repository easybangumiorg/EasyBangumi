package com.heyanle.easybangumi4.cartoon_local.source

import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class LocalSourceComponent: ComponentWrapper(), SearchComponent, PlayComponent, DetailedComponent {

    override suspend fun getAll(summary: CartoonSummary): SourceResult<Pair<Cartoon, List<PlayLine>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDetailed(summary: CartoonSummary): SourceResult<Cartoon> {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayLine(summary: CartoonSummary): SourceResult<List<PlayLine>> {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode
    ): SourceResult<PlayerInfo> {
        TODO("Not yet implemented")
    }

    override fun getFirstSearchKey(keyword: String): Int {
        TODO("Not yet implemented")
    }

    override suspend fun search(
        pageKey: Int,
        keyword: String
    ): SourceResult<Pair<Int?, List<CartoonCover>>> {
        TODO("Not yet implemented")
    }
}