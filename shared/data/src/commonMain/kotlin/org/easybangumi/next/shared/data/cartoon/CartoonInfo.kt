package org.easybangumi.next.shared.data.cartoon

import androidx.room.Entity
import kotlinx.datetime.Clock


/**
 * 番剧总包，聚合了一部番剧的所有数据
 * 1. 番剧最小标识（主键）  →   id, metaSourceKey
 *
 * 2. CartoonCover      ↘
 *                           来自播放源的数据
 * 3. Lise<PlayLine>    ↗
 *
 * 4. CartoonMata   →   番剧元数据，交给另一个模块通过外键指定
 *
 * 5. 收藏番剧相关数据
 * 6. 历史记录相关数据
 * 7. 其他数据 （如排序方式存储）
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
@Entity(tableName = "CartoonInfo", primaryKeys = ["id", "metaSourceKey"])
data class CartoonInfo(

    // finder
    val id: String,              // 标识，由源自己支持，用于区分番剧
    // 所属的元数据源 Key
    val metaSourceKey: String,

    // cartoonCover
    val name: String,
    val coverUrl: String,
    val detailedUrl: String,

    val isUpdate: Boolean = false,
    val lastUpdateTime: Long = 0L, // 最后更新的时间

    // preferences
    val isShowLine: Boolean = false,
    val sourceName: String = "",     // 源名称
    val reversal: Boolean = false, // 是否反转集数
    val sortByKey: String = "", // 排序名称
    val playLineString: String = "",             // List<PlayLine> 的 jsonl 数据，可能为 ""

    // star
    val tagsIdListString: String = "", // 番剧分类 "1, 2, 3" 的格式
    val starTime: Long = 0, // 收藏时间，为 0 则为未收藏
    val pinTime: Long = 0, // 置顶时间，为 0 则不置顶


    // history
    val lastHistoryTime: Long = 0, // 如果为 0 则代表没有历史记录

    val lastHistoryPlayerSourceKey: String = "",
    val lastHistoryPlayLineListJson: String = "",

    val lastLineId: String = "",
    val lastLineIndex: Int = 0,

    val lastEpisodeId: String = "",
    val lastEpisodeIndex: Int = 0,
    val lastEpisodeOrder: Int = 0,

    val lastEpisodeNum: Int = 0, // 最后一次观看时的总集数

    val mataId: String = "", // 对应元数据库数据的 id，具体数据交给另一个模块

    // other data
    // 扩展字段，帮源缓存，这里只是用于持久化，可能会过时
    // 业务需要在 extController 中获取
    val playSourceExt: String = "",
    val mataSourceExt: String = "",
    val createTime: Long = Clock.System.now().toEpochMilliseconds(), // 创建时间


) {

    val tagList: List<String> by lazy {
        if (tagsIdListString.isEmpty()) {
            emptyList()
        } else {
            tagsIdListString.split(",").map { it.trim() }.filterNot { it.isBlank() }.distinct()
        }
    }

    fun renameTag(o: String, n: String): CartoonInfo {
        if (tagList.contains(o) && !tagList.contains(n)) {
            return copy(tagsIdListString = tagList.joinToString(",") { if (it == o) n else it })
        }
        return this
    }


    fun toIdentify(): String {
        return "${id},${metaSourceKey}"
    }

    fun match(identify: String): Boolean {
        return this.toIdentify() == identify
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CartoonInfo

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
        if (metaSourceKey != other.metaSourceKey) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false
        if (detailedUrl != other.detailedUrl) return false
        if (sourceName != other.sourceName) return false
        if (sortByKey != other.sortByKey) return false
        if (playLineString != other.playLineString) return false
        if (tagsIdListString != other.tagsIdListString) return false
        if (lastHistoryPlayerSourceKey != other.lastHistoryPlayerSourceKey) return false
        if (lastHistoryPlayLineListJson != other.lastHistoryPlayLineListJson) return false
        if (lastLineId != other.lastLineId) return false
        if (lastEpisodeId != other.lastEpisodeId) return false
        if (mataId != other.mataId) return false
        if (playSourceExt != other.playSourceExt) return false
        if (mataSourceExt != other.mataSourceExt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isUpdate.hashCode()
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
        result = 31 * result + metaSourceKey.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        result = 31 * result + detailedUrl.hashCode()
        result = 31 * result + sourceName.hashCode()
        result = 31 * result + sortByKey.hashCode()
        result = 31 * result + playLineString.hashCode()
        result = 31 * result + tagsIdListString.hashCode()
        result = 31 * result + lastHistoryPlayerSourceKey.hashCode()
        result = 31 * result + lastHistoryPlayLineListJson.hashCode()
        result = 31 * result + lastLineId.hashCode()
        result = 31 * result + lastEpisodeId.hashCode()
        result = 31 * result + mataId.hashCode()
        result = 31 * result + playSourceExt.hashCode()
        result = 31 * result + mataSourceExt.hashCode()
        return result
    }


}