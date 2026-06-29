package org.easybangumi.next.shared.source

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.EpisodeSimple
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayLineSimple
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.play.PlayComponent

/**
 * PlayComponent 的扩展函数，简化 ComponentBusiness 的调用
 */

/**
 * 判断是否为剧集优先模式（无重试）
 */
suspend fun ComponentBusiness<PlayComponent>.isEpisodeFirstMode(
    cartoonIndex: CartoonIndex
): Boolean {
    return runNoRetry { isEpisodeFirstMode(cartoonIndex) }
}

/**
 * 获取剧集列表（带重试）
 */
suspend fun ComponentBusiness<PlayComponent>.getEpisodeList(
    cartoonIndex: CartoonIndex
): DataState<List<EpisodeSimple>>? {
    return runOrNull { getEpisodeList(cartoonIndex) }
}

/**
 * 根据剧集获取播放线路（带重试）
 */
suspend fun ComponentBusiness<PlayComponent>.getPlayLineSimpleForEpisode(
    cartoonIndex: CartoonIndex,
    episode: EpisodeSimple
): DataState<List<PlayLineSimple>>? {
    return runOrNull { getPlayLineSimpleForEpisode(cartoonIndex, episode) }
}

/**
 * 获取播放信息（带重试）
 */
suspend fun ComponentBusiness<PlayComponent>.getPlayInfoSimple(
    cartoonIndex: CartoonIndex,
    playLineSimple: PlayLineSimple,
    episodeSimple: EpisodeSimple
): DataState<PlayInfo>? {
    return runOrNull { getPlayInfoSimple(cartoonIndex, playLineSimple, episodeSimple) }
}
