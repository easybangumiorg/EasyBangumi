package org.easybangumi.next.source.inner.anich

import org.easybangumi.next.shared.data.cartoon.PlayLineSimple

/**
 * AniCh 任务类型
 */
sealed class AniChTask {
    /**
     * 搜索任务
     * @param keyword 搜索关键词
     */
    data class Search(val keyword: String) : AniChTask()

    /**
     * 获取剧集列表任务
     * @param bangumiId 番剧 ID
     */
    data class GetEpisodes(val bangumiId: String) : AniChTask()

    /**
     * 获取播放线路任务
     * @param bangumiId 番剧 ID
     * @param episode 集数（从1开始）
     */
    data class GetPlayLines(val bangumiId: String, val episode: Int) : AniChTask()

    /**
     * 获取播放地址任务
     * @param bangumiId 番剧 ID
     * @param episode 集数（从1开始）
     * @param playLineSimple 播放线路信息
     */
    data class GetPlayUrl(
        val bangumiId: String,
        val episode: Int,
        val playLineSimple: PlayLineSimple
    ) : AniChTask()

    companion object {
        /**
         * 根据任务类型获取所需的目标页面状态
         * @return 目标页面状态
         */
        fun AniChTask.getTargetPageState(): AniChPageState {
            return when (this) {
                is Search -> AniChPageState.Home
                is GetEpisodes -> AniChPageState.Detail(bangumiId)
                is GetPlayLines -> AniChPageState.Detail(bangumiId)
                is GetPlayUrl -> AniChPageState.Play(bangumiId, episode)
            }
        }
    }
}