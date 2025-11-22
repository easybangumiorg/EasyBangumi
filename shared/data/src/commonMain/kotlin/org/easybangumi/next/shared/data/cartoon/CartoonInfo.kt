package org.easybangumi.next.shared.data.cartoon

import androidx.room.Entity
import kotlinx.datetime.Clock
import org.easybangumi.next.lib.utils.getMatchReg


/**
 * 番剧总包，聚合了一部番剧的所有数据
 *
 * 一部番需要挂载在某个源上，作为唯一区分
 * 但实际上，番剧的 播放，下载，元数据，云端收藏，弹幕等可能来自其他源
 *
 * 例如
 * 在 bangumi 首页中点进的番 A 挂载在 bangumi 源
 *      播放数据可能来自 次元城
 *      弹幕数据可能来自 b站
 *      元数据可能来自 bangumi
 *      可能同时收藏在 bangumi 在看分类，本地某分类与 webdav 云端某分类
 *
 * 在 次元城 首页中点进的番 B 挂载在 次元城源
 *      播放数据可能来自 次元城 （大概率）
 *      弹幕数据可能来自 b站
 *      元数据可能来自 次元城 （可手动绑定到 bangumi 元数据）
 *      可能同时收藏在 本地某分类与 webdav 云端某分类
 *
 * 而番 B 可通过迁移功能，通过手动绑定 id 或搜索绑定的方式迁移到挂载 bangumi
 *
 * 而某些功能必须要挂载在源下才能使用，
 * 例如 ：
 * bangumi 的云端收藏分类只能收藏挂载在 bangumi 的番，
 * bangumi 的进度记录也只能在 bangumi 源下生效
 *
 * 1. 番剧最小标识（主键）
 *    id 含义由 挂载源 管理，源需要保证唯一
 *    fromSourceKey 为该番实体挂载的源 key
 *
 * 2. CartoonCover  →  来自挂载源的封面元数据
 *
 * 3. Star   →   收藏或置顶，包括云端分类和本地分类
 *
 * 4. CartoonMata   →   番剧元数据，通过外键关联，交给元数据管理模块指定
 *
 * 5. 历史记录相关数据
 *
 * 6. 其他数据 （如排序方式存储）
 *
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
@Entity(tableName = "CartoonInfo", primaryKeys = ["fromId", "fromSourceKey"])
data class CartoonInfo(

    // ================ from source ================
    // finder
    val fromId: String,              // 标识，由源自己支持，用于区分番剧
    // 来源 Source Key
    val fromSourceKey: String,

    // cartoonCover
    val name: String,
    val coverUrl: String,
    val detailedUrl: String,

    // preferences
    val isShowLine: Boolean = false,
    val sourceName: String = "",     // 源名称
    val reversal: Boolean = false, // 是否反转集数
    val sortByKey: String = "", // 排序名称
    val playLineString: String = "",             // List<PlayLine> 的 jsonl 数据，可能为 ""

    val lastUpdateTime: Long = 0, // 番剧更新时间，为 0 代表无更新

    // star
    val tagsIdListString: String = "", // 番剧分类 "1, 2, 3" 的格式
    val starTime: Long = 0, // 收藏时间，为 0 则为未收藏
    val pinTime: Long = 0, // 置顶时间，为 0 则不置顶


    // history
    val lastPlaySourceId: String = "", // 最后一次播放的源中该番的 id
    val lastPlaySourceKey: String = "", // 最后一次播放的源 key

    val lastHistoryTime: Long = 0, // 如果为 0 则代表没有历史记录
    val lastHistoryPlayLineListJson: String = "",

    val lastLineId: String = "",
    val lastLineIndex: Int = 0,

    val lastEpisodeId: String = "",
    val lastEpisodeIndex: Int = 0,
    val lastEpisodeOrder: Int = 0,

    val lastEpisodeNum: Int = 0, // 最后一次观看时的总集数
    val lastEpisodeLabel: String = "", // 最后一次观看的集数标签
    val lastProcessTime: Long = 0, // 最后一次观看进度时间点，单位毫秒

    // other data
    // 扩展字段，帮源缓存，这里只是用于持久化，可能会过时
    // 业务需要在 extController 中获取
    val fromSourceExt: String = "",
    val playSourceExt: String = "",
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
        return "${fromId},${fromSourceKey}"
    }

    fun toCartoonIndex(): CartoonIndex {
        return CartoonIndex(
            id = fromId,
            source = fromSourceKey,
        ).apply {
            ext = this@CartoonInfo.fromSourceExt
        }
    }
    fun toCartoonCover(): CartoonCover {
        return CartoonCover(
            id = fromId,
            source = fromSourceKey,
            name = name,
            coverUrl = coverUrl,
            intro = "",
            webUrl = detailedUrl
        )
    }

    fun match(identify: String): Boolean {
        return this.toIdentify() == identify
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

    fun isPin() = pinTime > 0


}