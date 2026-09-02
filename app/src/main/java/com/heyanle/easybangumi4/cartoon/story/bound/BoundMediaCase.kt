package com.heyanle.easybangumi4.cartoon.story.bound

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine

data class BoundMedia(
    val uri: String,
    val displayName: String,
    val binding: CartoonEpisodeBinding,
)

/**
 * 集级绑定的查询与写入入口：
 * 播放侧用它把远程 (summary, line, episode) 解析成本地媒体 URI；
 * 下载完成后自动写入绑定，用户也可在播放页手动换绑。
 */
class BoundMediaCase(
    private val bindingController: CartoonEpisodeBindingController,
    private val flatDownloadController: FlatDownloadController,
    private val cartoonStoryController: CartoonStoryController,
) {

    fun findBinding(
        summary: CartoonSummary,
        lineId: String?,
        episode: Episode,
    ): CartoonEpisodeBinding? {
        return bindingController.findBinding(
            source = summary.source,
            cartoonId = summary.id,
            lineId = lineId,
            episodeId = episode.id,
            episodeOrder = episode.order,
        )
    }

    suspend fun findLocalMedia(
        summary: CartoonSummary,
        lineId: String?,
        episode: Episode,
    ): BoundMedia? {
        val binding = findBinding(summary, lineId, episode) ?: return null
        return resolveMedia(binding)
    }

    suspend fun resolveMedia(binding: CartoonEpisodeBinding): BoundMedia? {
        return when (binding.targetType) {
            CartoonEpisodeBinding.TARGET_FLAT_FILE -> {
                val file = flatDownloadController.flatVideos.value.firstOrNull {
                    it.fileName == binding.flatFileName
                } ?: return null
                BoundMedia(
                    uri = file.uri,
                    displayName = file.fileName,
                    binding = binding,
                )
            }
            CartoonEpisodeBinding.TARGET_LOCAL_STORY -> {
                val item = (cartoonStoryController.storyItemList.value as? DataResult.Ok)?.data
                    ?.firstOrNull { it.cartoonLocalItem.itemId == binding.localItemId }
                    ?: return null
                val episodeItem = item.cartoonLocalItem.episodes.firstOrNull {
                    it.episode == binding.localEpisode
                } ?: return null
                BoundMedia(
                    uri = episodeItem.mediaUri,
                    displayName = episodeItem.title.ifBlank {
                        "${item.cartoonLocalItem.title} · 编号 ${binding.localEpisode}"
                    },
                    binding = binding,
                )
            }
            else -> null
        }
    }

    // 下载成功后的自动绑定
    fun bindDownloadedFlatFile(
        cartoonInfo: CartoonInfo,
        playLine: PlayLine,
        episode: Episode,
        flatFileName: String,
    ) {
        upsert(
            cartoonInfo, playLine, episode,
            targetType = CartoonEpisodeBinding.TARGET_FLAT_FILE,
            flatFileName = flatFileName,
            localItemId = "",
            localEpisode = 0,
            bindFrom = CartoonEpisodeBinding.FROM_DOWNLOAD,
        )
    }

    fun bindDownloadedLocalStory(
        cartoonInfo: CartoonInfo,
        playLine: PlayLine,
        episode: Episode,
        localItemId: String,
        localEpisode: Int,
    ) {
        upsert(
            cartoonInfo, playLine, episode,
            targetType = CartoonEpisodeBinding.TARGET_LOCAL_STORY,
            flatFileName = "",
            localItemId = localItemId,
            localEpisode = localEpisode,
            bindFrom = CartoonEpisodeBinding.FROM_DOWNLOAD,
        )
    }

    // 播放页手动换绑
    suspend fun manualBindFlatFile(
        cartoonInfo: CartoonInfo,
        playLine: PlayLine,
        episode: Episode,
        flatFileName: String,
    ) {
        upsert(
            cartoonInfo, playLine, episode,
            targetType = CartoonEpisodeBinding.TARGET_FLAT_FILE,
            flatFileName = flatFileName,
            localItemId = "",
            localEpisode = 0,
            bindFrom = CartoonEpisodeBinding.FROM_MANUAL,
        )
    }

    suspend fun manualBindLocalStory(
        cartoonInfo: CartoonInfo,
        playLine: PlayLine,
        episode: Episode,
        localItemId: String,
        localEpisode: Int,
    ) {
        upsert(
            cartoonInfo, playLine, episode,
            targetType = CartoonEpisodeBinding.TARGET_LOCAL_STORY,
            flatFileName = "",
            localItemId = localItemId,
            localEpisode = localEpisode,
            bindFrom = CartoonEpisodeBinding.FROM_MANUAL,
        )
    }

    fun unbind(source: String, cartoonId: String, episodeId: String, episodeOrder: Int) {
        bindingController.remove(source, cartoonId, episodeId, episodeOrder)
    }

    fun unbind(binding: CartoonEpisodeBinding) {
        bindingController.remove(
            binding.source, binding.cartoonId, binding.episodeId, binding.episodeOrder
        )
    }

    private fun upsert(
        cartoonInfo: CartoonInfo,
        playLine: PlayLine,
        episode: Episode,
        targetType: Int,
        flatFileName: String,
        localItemId: String,
        localEpisode: Int,
        bindFrom: Int,
    ) {
        bindingController.upsert(
            CartoonEpisodeBinding(
                source = cartoonInfo.source,
                cartoonId = cartoonInfo.id,
                cartoonTitle = cartoonInfo.name,
                cartoonCover = cartoonInfo.coverUrl,
                lineId = playLine.id,
                episodeId = episode.id,
                episodeOrder = episode.order,
                episodeLabel = episode.label,
                targetType = targetType,
                flatFileName = flatFileName,
                localItemId = localItemId,
                localEpisode = localEpisode,
                bindFrom = bindFrom,
                bindTime = System.currentTimeMillis(),
            )
        )
    }

}
