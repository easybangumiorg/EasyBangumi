package org.easybangumi.next.source.local

import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.withResult
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.local.LocalCartoonController
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.koin.core.component.inject

/**
 * 本地番剧播放组件
 */
class LocalPlayComponent : PlayComponent, BaseComponent() {

    private val localController: LocalCartoonController by inject()

    override suspend fun getPlayLines(cartoonIndex: CartoonIndex): DataState<List<PlayerLine>> {
        return withResult {
            val item = localController.findByCartoonId(cartoonIndex.id)
                ?: throw IllegalStateException("本地番剧不存在: ${cartoonIndex.id}")

            val episodes = item.episodes.map { ep ->
                Episode(
                    id = ep.episode.toString(),
                    label = ep.title.ifEmpty { "第${ep.episode}集" },
                    order = ep.episode,
                )
            }

            listOf(
                PlayerLine(
                    id = "local",
                    label = "本地",
                    episodeList = episodes,
                )
            )
        }
    }

    override suspend fun getPlayInfo(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode,
    ): DataState<PlayInfo> {
        return withResult {
            val item = localController.findByCartoonId(cartoonIndex.id)
                ?: throw IllegalStateException("本地番剧不存在: ${cartoonIndex.id}")

            val ep = item.episodes.find { it.episode.toString() == episode.id }
                ?: throw IllegalStateException("本地剧集不存在: ${episode.id}")

            val mediaFile = UniFileFactory.fromUFD(ep.mediaUFD)
                ?: throw IllegalStateException("媒体文件不存在")

            val filePath = mediaFile.getFilePath()
            val url = if (filePath.isNotEmpty()) filePath else mediaFile.getUri()

            PlayInfo(
                url = url,
                type = PlayInfo.TYPE_NORMAL,
            )
        }
    }
}
