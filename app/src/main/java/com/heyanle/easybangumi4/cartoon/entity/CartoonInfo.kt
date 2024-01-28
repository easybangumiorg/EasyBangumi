package com.heyanle.easybangumi4.cartoon.entity

import androidx.room.Entity
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonImpl
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.utils.getMatchReg
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson
import java.net.URLEncoder

/**
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
@Entity(tableName = "CartoonInfoV2", primaryKeys = ["id", "source"])
data class CartoonInfo(

    // finder
    val id: String,              // 标识，由源自己支持，用于区分番剧
    val source: String,

    // cartoonCover
    val name: String,
    val coverUrl: String,
    val intro: String,
    val url: String,

    // cartoon detailed
    val isDetailed: Boolean = false,
    val genre: String = "",          // 标签，为 "xx, xx"，标签 id
    val description: String = "",
    val updateStrategy: Int = Cartoon.UPDATE_STRATEGY_ALWAYS,
    val isUpdate: Boolean = false,
    val status: Int = Cartoon.STATUS_UNKNOWN,
    val lastUpdateTime: Long = 0L, // 最后更新的时间

    val isShowLine: Boolean = false,
    val sourceName: String = "",     // 源名称
    val reversal: Boolean = false, // 是否反转集数
    val sortByKey: String = "", // 排序名称

    val isPlayLineLoad: Boolean = false,
    val playLineString: String = "", // List<PlayLine> 的 json 数据，可能为 ""

    // star
    val tags: String = "", // 番剧分类 "1, 2, 3" 的格式
    val starTime: Long = 0, // 收藏时间，为 0 则为未收藏
    val upTime: Long = 0, // 置顶时间，为 0 则不置顶

    // history
    val lastHistoryTime: Long = 0, // 如果为 0 则代表没有历史记录
    val lastPlayLineEpisodeString: String = "", // 历史记录当前 play line 的 list<episode> 记录，已排序
    val lastLineId: String = "",
    val lastLinesIndex: Int = 0,
    val lastLineLabel: String = "",

    val lastEpisodeId: String = "",
    val lastEpisodeOrder: Int = 0,
    val lastEpisodeIndex: Int = 0,
    val lastEpisodeLabel: String = "",

    val lastTotalTile: Long = 0,
    val lastProcessTime: Long = 0,


    // other data
    val createTime: Long = System.currentTimeMillis(),
) {

    companion object {
        fun fromCartoonCover(cartoonCover: CartoonCover): CartoonInfo {
            return CartoonInfo(
                id = cartoonCover.id,
                source = cartoonCover.source,
                url = cartoonCover.url,
                name = cartoonCover.title,
                coverUrl = cartoonCover.coverUrl ?: "",
                intro = cartoonCover.intro ?: "",
            )
        }

        fun fromCartoon(
            cartoon: Cartoon,
            sourceName: String? = null,
            playLine: List<PlayLine>? = null,
        ): CartoonInfo {
            return CartoonInfo(
                id = cartoon.id,
                source = cartoon.source,
                url = cartoon.url,

                isDetailed = true,
                name = cartoon.title,
                coverUrl = cartoon.coverUrl ?: "",
                intro = cartoon.intro ?: "",

                genre = cartoon.genre ?: "",
                description = cartoon.description ?: "",

                updateStrategy = cartoon.updateStrategy,
                isUpdate = cartoon.isUpdate,
                status = cartoon.status,
                sourceName = sourceName ?: "",

                isShowLine = playLine !is DetailedComponent.NonPlayLine,
                isPlayLineLoad = playLine != null,
                playLineString = playLine?.toJson() ?: ""
            )
        }
    }


    val genres: List<String> by lazy {
        if (genre.isEmpty()) {
            emptyList<String>()
        } else {
            genre.split(",").map { it.trim() }.filterNot { it.isBlank() }.distinct()
        }
    }
    val playLine: List<PlayLine> by lazy {
        if (playLineString.isEmpty()) {
            emptyList()
        } else {
            playLineString.jsonTo() ?: emptyList()
        }
    }

    val playLineWrapper: List<PlayLineWrapper> by lazy {
        playLine.map {
            PlayLineWrapper.fromKey(it, sortByKey, reversal)
        }
    }


    val matchHistoryEpisode: Pair<PlayLineWrapper, Episode>? by lazy {
        /**
         * 使用状态压缩进行优先级匹配，o(n) 即可
         */
        var currentPlayLine: PlayLineWrapper? = null
        var currentPlayLineMask: Int = 0
        playLineWrapper.forEachIndexed { index, playLine ->
            var mask = 0
            if (lastLineId.isNotEmpty() && lastLineId == playLine.playLine.id) {
                mask = mask or 0b100
            }
            if (lastLineLabel.isNotEmpty() && lastLineLabel == playLine.playLine.label) {
                mask = mask or 0b010
            }
            if (lastLinesIndex >= 0 && lastLinesIndex == index) {
                mask = mask or 0b001
            }
            if (mask > currentPlayLineMask) {
                currentPlayLine = playLine
                currentPlayLineMask = mask
            }
        }

        // 匹配不到播放线路直接返回 null，使用降级（历史记录或兜底）
        if (currentPlayLine == null) {
            return@lazy null
        }
        var currentEpisode: Episode? = null
        var currentEpisodeMask = 0
        currentPlayLine?.playLine?.episode?.forEachIndexed { index, episode ->
            var mask = 0
            if (lastEpisodeId.isNotEmpty() && lastEpisodeId == episode.id) {
                mask = mask or 0b1000
            }
            if (lastEpisodeOrder >= 0 && lastEpisodeOrder == episode.order) {
                mask = mask or 0b0100
            }
            if (lastEpisodeLabel.isNotEmpty() && lastEpisodeLabel == episode.label) {
                mask = mask or 0b0010
            }
            if (lastEpisodeIndex >= 0 && lastEpisodeIndex == index) {
                mask = mask or 0b0001
            }
            if (mask > currentEpisodeMask) {
                currentEpisode = episode
                currentEpisodeMask = mask
            }
        }
        return@lazy (currentPlayLine ?: return@lazy null) to (currentEpisode ?: return@lazy null)
    }

    fun matches(query: String): Boolean {
        var matched = false
        for (match in query.split(',')) {
            val regex = match.getMatchReg()
            if (name.matches(regex)) {
                matched = true
                break
            }
        }
        return matched
    }

    fun getSummary(): CartoonSummary {
        return CartoonSummary(id, source)
    }

    fun toIdentify(): String {
        return "${id},${source}"
    }

    fun match(identify: String): Boolean {
        return this.toIdentify() == identify
    }

    fun match(cartoon: Cartoon): Boolean {
        return this.id == cartoon.id && this.source == cartoon.source
    }

    fun match(cartoon: CartoonCover): Boolean {
        return this.id == cartoon.id && this.source == cartoon.source
    }

    fun match(cartoon: CartoonSummary): Boolean {
        return this.id == cartoon.id && this.source == cartoon.source
    }

    fun isUp() = upTime != 0L


    fun toSummary(): CartoonSummary {
        return CartoonSummary(id, source)
    }

    fun toCartoon(): Cartoon {
        return CartoonImpl(
            id,
            source,
            url,
            name,
            genre,
            coverUrl,
            intro,
            description,
            updateStrategy,
            isUpdate,
            status
        )
    }

    fun copyFromCartoon(
        cartoon: Cartoon,
        sourceName: String? = null,
        playLine: List<PlayLine>? = null,
    ): CartoonInfo {
        var isUpdate = cartoon.isUpdate
        if (playLine != null) {
            val old = this.playLine
            if (old.size != playLine.size) {
                isUpdate = true
            } else {
                for (i in 0 until playLine.size.coerceAtMost(old.size)) {
                    if (playLine[i].episode.size != old[i].episode.size) {
                        isUpdate = true
                        break
                    }
                }
            }
        }




        return copy(
            id = cartoon.id,
            source = cartoon.source,
            url = cartoon.url,
            lastUpdateTime = System.currentTimeMillis(),

            name = cartoon.title,
            coverUrl = cartoon.coverUrl ?: "",
            intro = cartoon.intro ?: "",

            isDetailed = true,
            genre = cartoon.genre ?: "",
            description = cartoon.description ?: "",

            updateStrategy = cartoon.updateStrategy,
            isUpdate = isUpdate,
            status = cartoon.status,
            sourceName = sourceName ?: this.sourceName,

            isShowLine = if(playLine == null) isShowLine else playLine !is DetailedComponent.NonPlayLine,
            isPlayLineLoad = playLine != null,
            playLineString = playLine?.toJson() ?: playLineString
        )
    }

    fun copyHistory(
        playLineIndex: Int,
        playLine: PlayLine,
        episode: Episode,
        process: Long,
    ): CartoonInfo {
        return copy(
            lastHistoryTime = System.currentTimeMillis(), // 如果为 0 则代表没有历史记录
            lastLineId = playLine.id,
            lastLinesIndex = playLineIndex,
            lastLineLabel = playLine.label,

            lastEpisodeId = episode.id,
            lastEpisodeOrder = episode.order,
            lastEpisodeIndex = playLine.episode.indexOf(episode).coerceAtLeast(0),
            lastEpisodeLabel = episode.label,

            lastProcessTime = process,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartoonInfo

        if (id != other.id) return false
        if (source != other.source) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false
        if (intro != other.intro) return false
        if (url != other.url) return false
        if (isDetailed != other.isDetailed) return false
        if (genre != other.genre) return false
        if (description != other.description) return false
        if (updateStrategy != other.updateStrategy) return false
        if (isUpdate != other.isUpdate) return false
        if (status != other.status) return false
        if (lastUpdateTime != other.lastUpdateTime) return false
        if (isShowLine != other.isShowLine) return false
        if (sourceName != other.sourceName) return false
        if (reversal != other.reversal) return false
        if (sortByKey != other.sortByKey) return false
        if (isPlayLineLoad != other.isPlayLineLoad) return false
        if (playLineString != other.playLineString) return false
        if (tags != other.tags) return false
        if (starTime != other.starTime) return false
        if (upTime != other.upTime) return false
        if (lastHistoryTime != other.lastHistoryTime) return false
        if (lastPlayLineEpisodeString != other.lastPlayLineEpisodeString) return false
        if (lastLineId != other.lastLineId) return false
        if (lastLinesIndex != other.lastLinesIndex) return false
        if (lastLineLabel != other.lastLineLabel) return false
        if (lastEpisodeId != other.lastEpisodeId) return false
        if (lastEpisodeOrder != other.lastEpisodeOrder) return false
        if (lastEpisodeIndex != other.lastEpisodeIndex) return false
        if (lastEpisodeLabel != other.lastEpisodeLabel) return false
        if (lastTotalTile != other.lastTotalTile) return false
        if (lastProcessTime != other.lastProcessTime) return false
        return createTime == other.createTime
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        result = 31 * result + intro.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + isDetailed.hashCode()
        result = 31 * result + genre.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + updateStrategy
        result = 31 * result + isUpdate.hashCode()
        result = 31 * result + status
        result = 31 * result + lastUpdateTime.hashCode()
        result = 31 * result + isShowLine.hashCode()
        result = 31 * result + sourceName.hashCode()
        result = 31 * result + reversal.hashCode()
        result = 31 * result + sortByKey.hashCode()
        result = 31 * result + isPlayLineLoad.hashCode()
        result = 31 * result + playLineString.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + starTime.hashCode()
        result = 31 * result + upTime.hashCode()
        result = 31 * result + lastHistoryTime.hashCode()
        result = 31 * result + lastPlayLineEpisodeString.hashCode()
        result = 31 * result + lastLineId.hashCode()
        result = 31 * result + lastLinesIndex
        result = 31 * result + lastLineLabel.hashCode()
        result = 31 * result + lastEpisodeId.hashCode()
        result = 31 * result + lastEpisodeOrder
        result = 31 * result + lastEpisodeIndex
        result = 31 * result + lastEpisodeLabel.hashCode()
        result = 31 * result + lastTotalTile.hashCode()
        result = 31 * result + lastProcessTime.hashCode()
        result = 31 * result + createTime.hashCode()
        return result
    }


}