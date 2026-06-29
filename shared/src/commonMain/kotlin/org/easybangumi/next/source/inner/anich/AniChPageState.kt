package org.easybangumi.next.source.inner.anich

/**
 * AniCh 页面状态，用于管理浏览器当前所在页面
 */
sealed class AniChPageState {
    /**
     * 初始状态，未加载任何页面
     */
    object Idle : AniChPageState()

    /**
     * 首页状态
     */
    object Home : AniChPageState()

    /**
     * 详情页状态
     * @param bangumiId 番剧 ID
     */
    data class Detail(val bangumiId: String) : AniChPageState()

    /**
     * 播放页状态
     * @param bangumiId 番剧 ID
     * @param episode 集数（从1开始）
     */
    data class Play(val bangumiId: String, val episode: Int) : AniChPageState()

    companion object {
        /**
         * 根据 URL 检测当前页面状态
         * @param url 当前页面 URL
         * @return 检测到的页面状态
         */
        fun fromUrl(url: String): AniChPageState {
            // 首页：根路径或空路径
            if (url.matches(Regex("https?://[^/]+/?$"))) {
                return Home
            }
            // 详情页：/b/{id}
            val detailPattern = Regex("https?://[^/]+/b/([^/?]+)")
            detailPattern.find(url)?.let { match ->
                return Detail(match.groupValues[1])
            }
            // 播放页：/b/{id}/{episode}
            val playPattern = Regex("https?://[^/]+/b/([^/?]+)/([0-9]+)")
            playPattern.find(url)?.let { match ->
                val bangumiId = match.groupValues[1]
                val episode = match.groupValues[2].toIntOrNull() ?: 1
                return Play(bangumiId, episode)
            }
            // 其他页面（如搜索页）
            return Home
        }
    }
}