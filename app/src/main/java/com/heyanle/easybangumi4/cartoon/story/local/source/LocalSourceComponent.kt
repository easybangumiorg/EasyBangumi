package com.heyanle.easybangumi4.cartoon.story.local.source

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.base.toDataResult
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.easybangumi4.plugin.source.SourceException
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonImpl
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.source_api.withResult
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class LocalSourceComponent : ComponentWrapper(), PlayComponent, DetailedComponent {

    val cartoonStoryController: CartoonStoryController by Inject.injectLazy()

    override suspend fun getAll(summary: CartoonSummary): SourceResult<Pair<Cartoon, List<PlayLine>>> {
        return withResult {
            val cartoon = getDetailed(summary)
            if (cartoon is SourceResult.Error) {
                throw cartoon.throwable
            }
            val playLine = getPlayLine(summary)
            if (playLine is SourceResult.Error) {
                throw playLine.throwable
            }
            val c = cartoon.toDataResult().okOrNull() ?: throw IllegalStateException()
            val p = playLine.toDataResult().okOrNull() ?: throw IllegalStateException()
            return@withResult c to p

        }
    }

    override suspend fun getDetailed(summary: CartoonSummary): SourceResult<Cartoon> {
        return withResult {
            val result = cartoonStoryController.storyItemList.filter { it !is DataResult.Error }
                .first()
            when (result) {
                is DataResult.Ok -> {
                    val it = result.data.firstOrNull() { it.cartoonLocalItem.itemId == summary.id }
                        ?: throw SourceException("local cartoon not found")
                    return@withResult CartoonImpl(
                        id = summary.id,
                        source = LocalSource.LOCAL_SOURCE_KEY,
                        url = "",

                        title = it.cartoonLocalItem.title,
                        genre = it.cartoonLocalItem.genre.joinToString(", "),

                        coverUrl = it.cartoonLocalItem.cartoonCover.coverUrl,
                        intro = "",
                        description = it.cartoonLocalItem.desc,
                        updateStrategy = Cartoon.UPDATE_STRATEGY_NEVER,
                        isUpdate = false,
                        status = Cartoon.STATUS_UNKNOWN
                    )

                }

                is DataResult.Error -> {
                    throw result.throwable ?: SourceException(result.errorMsg)
                }

                else -> {
                    throw IllegalStateException()
                }
            }
        }

    }

    override suspend fun getPlayLine(summary: CartoonSummary): SourceResult<List<PlayLine>> {
        return withResult {
            val result = cartoonStoryController.storyItemList.filter { it !is DataResult.Error }
                .first()
            when (result) {
                is DataResult.Ok -> {
                    val it = result.data.firstOrNull() { it.cartoonLocalItem.itemId == summary.id }
                        ?: throw SourceException("local cartoon not found")

                    val episodeList = arrayListOf<Episode>()
                    it.cartoonLocalItem.episodes.forEach {
                        episodeList.add(
                            Episode(
                                id = it.title + it.episode,
                                order = it.episode,
                                label = it.title
                            )
                        )
                    }
                    return@withResult DetailedComponent.NonPlayLine(
                        PlayLine(
                            "", "", episodeList
                        )
                    )

                }

                is DataResult.Error -> {
                    throw result.throwable ?: SourceException(result.errorMsg)
                }

                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }

    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode
    ): SourceResult<PlayerInfo> {
        return withResult {
            val result = cartoonStoryController.storyItemList.filter { it !is DataResult.Error }
                .first()
            when (result) {
                is DataResult.Ok -> {
                    val it = result.data.firstOrNull() { it.cartoonLocalItem.itemId == summary.id }
                        ?: throw SourceException("local cartoon not found")
                    val e = it.cartoonLocalItem.episodes.firstOrNull { it.episode == episode.order }
                        ?: throw SourceException("episode not found")

                    return@withResult PlayerInfo(
                        decodeType = PlayerInfo.DECODE_TYPE_OTHER,
                        uri = e.mediaUri
                    )
                }

                is DataResult.Error -> {
                    throw result.throwable ?: SourceException(result.errorMsg)
                }

                else -> {
                    throw IllegalStateException()
                }
            }

        }
    }


}