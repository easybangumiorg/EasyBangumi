package com.heyanle.easybangumi4.cartoon.story.bound

/**
 * 集级绑定：番剧实体的某一集 ↔ 本地媒体。
 * 本地媒体可以是扁平下载目录里的某个视频文件，也可以是现有本地番源条目的某一集。
 * 绑定是显式记录，不做文件名刮削猜测。
 */
data class CartoonEpisodeBinding(
    // 番剧实体身份
    val source: String,
    val cartoonId: String,
    // 番剧实体快照，供扁平目录 tab 与换绑弹窗展示
    val cartoonTitle: String,
    val cartoonCover: String,

    // 源剧集身份，匹配优先级 episodeId -> episodeOrder
    val lineId: String,
    val episodeId: String,
    val episodeOrder: Int,
    val episodeLabel: String,

    // 本地目标
    val targetType: Int,
    val flatFileName: String,   // targetType == TARGET_FLAT_FILE
    val localItemId: String,    // targetType == TARGET_LOCAL_STORY
    val localEpisode: Int,      // targetType == TARGET_LOCAL_STORY

    val bindFrom: Int,
    val bindTime: Long,
) {

    companion object {
        const val TARGET_FLAT_FILE = 0
        const val TARGET_LOCAL_STORY = 1

        const val FROM_DOWNLOAD = 0
        const val FROM_MANUAL = 1
    }

    fun sameEpisode(other: CartoonEpisodeBinding): Boolean {
        if (source != other.source || cartoonId != other.cartoonId) return false
        if (episodeId.isNotEmpty() && other.episodeId.isNotEmpty()) {
            return episodeId == other.episodeId
        }
        return episodeOrder == other.episodeOrder
    }

    fun isSameEpisode(source: String, cartoonId: String, episodeId: String, episodeOrder: Int): Boolean {
        if (this.source != source || this.cartoonId != cartoonId) return false
        if (this.episodeId.isNotEmpty() && episodeId.isNotEmpty()) {
            return this.episodeId == episodeId
        }
        return this.episodeOrder == episodeOrder
    }
}
