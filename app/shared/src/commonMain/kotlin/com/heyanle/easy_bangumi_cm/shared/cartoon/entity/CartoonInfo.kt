package com.heyanle.easy_bangumi_cm.shared.cartoon.entity

import androidx.room.Entity
import com.heyanle.easy_bangumi_cm.shared.utils.getMatchReg


/**
 * 番剧总包，聚合了一部番剧的所有数据
 * 1. 番剧最小标识（主键）  →   id, mediaSourceKey
 *
 * 2. CartoonCover      ↘
 * 3. CartoonDetailed   →   来自播放源的数据
 * 4. Lise<PlayLine>    ↗
 *
 * 5. CartoonMata   →   来自元数据源的数据
 *
 * 6. 收藏番剧相关数据
 * 7. 历史记录相关数据
 * 8. 其他数据 （如排序方式存储）
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
@Entity(tableName = "CartoonInfo", primaryKeys = ["id", "mediaSourceKey"])
data class CartoonInfo(

    // finder
    val id: String,              // 标识，由源自己支持，用于区分番剧
    // 所属的播放源 Key
    val mediaSourceKey: String,

    // cartoonCover
    val name: String,
    val coverUrl: String,
    val detailedUrl: String,

    // cartoon detailed
    val isDetailedLoaded: Boolean = false,
    val genre: String = "",          // 标签，为 "xx, xx"，标签 id
    val description: String = "",
    val isUpdate: Boolean = false,
    val lastUpdateTime: Long = 0L, // 最后更新的时间

    val isShowLine: Boolean = false,
    val sourceName: String = "",     // 源名称
    val reversal: Boolean = false, // 是否反转集数
    val sortByKey: String = "", // 排序名称
    val playLineString: String = "",             // List<PlayLine> 的 jsonl 数据，可能为 ""

    // star
    val tags: String = "", // 番剧分类 "1, 2, 3" 的格式
    val starTime: Long = 0, // 收藏时间，为 0 则为未收藏
    val pinTime: Long = 0, // 置顶时间，为 0 则不置顶



    // history
    val lastHistoryTime: Long = 0, // 如果为 0 则代表没有历史记录

    val lastLineId: String = "",
    val lastLineIndex: Int = 0,

    val lastEpisodeId: String = "",
    val lastEpisodeIndex: Int = 0,
    val lastEpisodeOrder: Int = 0,

    val lastEpisodeNum: Int = 0, // 最后一次观看时的总集数

    // sourceMata
    val mataSourceKey: String = "",
    val cartoonMataId: String = "", // 对应元数据库数据的 id，具体数据交给另一个库

    // other data
    // 扩展字段，帮源缓存，这里只是用于持久化，可能会过时
    // 业务需要在 extController 中获取
    val mediaExt: String = "",
    val mataSourceExt: String = "",
    val createTime: Long = System.currentTimeMillis(),

    // temp


) {

    val genres: List<String> by lazy {
        if (genre.isEmpty()) {
            emptyList<String>()
        } else {
            genre.split(",").map { it.trim() }.filterNot { it.isBlank() }.distinct()
        }
    }

    val tagList: List<String> by lazy {
        if (tags.isEmpty()) {
            emptyList()
        } else {
            tags.split(",").map { it.trim() }.filterNot { it.isBlank() }.distinct()
        }
    }

    fun renameTag(o: String, n: String): CartoonInfo {
        if (tagList.contains(o) && !tagList.contains(n)) {
            return copy(tags = tagList.map { if (it == o) n else it }.joinToString(","))
        }
        return this
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

    fun toIdentify(): String {
        return "${id},${mediaSourceKey}"
    }

    fun match(identify: String): Boolean {
        return this.toIdentify() == identify
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartoonInfo

        if (isDetailedLoaded != other.isDetailedLoaded) return false
        if (isUpdate != other.isUpdate) return false
        if (lastUpdateTime != other.lastUpdateTime) return false
        if (isShowLine != other.isShowLine) return false
        if (reversal != other.reversal) return false
        if (starTime != other.starTime) return false
        if (pinTime != other.pinTime) return false
        if (lastHistoryTime != other.lastHistoryTime) return false
        if (lastLineIndex != other.lastLineIndex) return false
        if (lastEpisodeIndex != other.lastEpisodeIndex) return false
        if (lastEpisodeOrder != other.lastEpisodeOrder) return false
        if (lastEpisodeNum != other.lastEpisodeNum) return false
        if (createTime != other.createTime) return false
        if (id != other.id) return false
        if (mediaSourceKey != other.mediaSourceKey) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false
        if (detailedUrl != other.detailedUrl) return false
        if (genre != other.genre) return false
        if (description != other.description) return false
        if (sourceName != other.sourceName) return false
        if (sortByKey != other.sortByKey) return false
        if (playLineString != other.playLineString) return false
        if (tags != other.tags) return false
        if (lastLineId != other.lastLineId) return false
        if (lastEpisodeId != other.lastEpisodeId) return false
        if (mataSourceKey != other.mataSourceKey) return false
        if (cartoonMataId != other.cartoonMataId) return false
        if (mediaExt != other.mediaExt) return false
        if (mataSourceExt != other.mataSourceExt) return false
        if (genres != other.genres) return false
        if (tagList != other.tagList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isDetailedLoaded.hashCode()
        result = 31 * result + isUpdate.hashCode()
        result = 31 * result + lastUpdateTime.hashCode()
        result = 31 * result + isShowLine.hashCode()
        result = 31 * result + reversal.hashCode()
        result = 31 * result + starTime.hashCode()
        result = 31 * result + pinTime.hashCode()
        result = 31 * result + lastHistoryTime.hashCode()
        result = 31 * result + lastLineIndex
        result = 31 * result + lastEpisodeIndex
        result = 31 * result + lastEpisodeOrder
        result = 31 * result + lastEpisodeNum
        result = 31 * result + createTime.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + mediaSourceKey.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        result = 31 * result + detailedUrl.hashCode()
        result = 31 * result + genre.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + sourceName.hashCode()
        result = 31 * result + sortByKey.hashCode()
        result = 31 * result + playLineString.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + lastLineId.hashCode()
        result = 31 * result + lastEpisodeId.hashCode()
        result = 31 * result + mataSourceKey.hashCode()
        result = 31 * result + cartoonMataId.hashCode()
        result = 31 * result + mediaExt.hashCode()
        result = 31 * result + mataSourceExt.hashCode()
        result = 31 * result + genres.hashCode()
        result = 31 * result + tagList.hashCode()
        return result
    }


}