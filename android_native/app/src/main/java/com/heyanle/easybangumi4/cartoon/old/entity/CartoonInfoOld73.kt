package com.heyanle.easybangumi4.cartoon.old.entity

import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonImpl
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.utils.getMatchReg
import java.net.URLEncoder

/**
 * Created by HeYanLe on 2023/8/13 16:29.
 * https://github.com/heyanLE
 */
@Entity(tableName = "CartoonInfo", primaryKeys = ["id", "source", "url"])
data class CartoonInfoOld(
    var id: String,              // 标识，由源自己支持，用于区分番剧

    var source: String,

    var url: String,

    var title: String,

    var genre: String,          // 标签，为 "xx, xx"，源 id

    var sourceName: String,     // 源名称

    var coverUrl: String,

    var intro: String,

    var description: String,

    var updateStrategy: Int,

    var isUpdate: Boolean,

    var status: Int,

    var tags: String, // 番剧分类 "1, 2, 3" 的格式

    var createTime: Long = System.currentTimeMillis(),

    var isShowLine: Boolean,

    var playLineString: String, // List<PlayLine> 的 json 数据，可能为 ""

    var lastUpdateTime: Long = 0L,


) {
    @Ignore
    private var genres: List<String>? = null

    fun getGenres(): List<String>? {
        if (genre.isEmpty()) {
            return null
        }
        if (genres == null) {
            genres = genre.split(",").map { it.trim() }.filterNot { it.isBlank() }.distinct()
        }
        return genres
    }

    companion object {
        fun fromCartoon(
            cartoon: Cartoon,
            sourceName: String,
            playLines: List<PlayLine>
        ): CartoonInfoOld {
            return CartoonInfoOld(
                id = cartoon.id,
                source = cartoon.source,
                url = cartoon.url,
                title = cartoon.title,
                genre = cartoon.genre ?: "",
                coverUrl = cartoon.coverUrl ?: "",
                intro = cartoon.intro ?: "",
                description = cartoon.description ?: "",
                updateStrategy = cartoon.updateStrategy,
                status = cartoon.status,
                playLineString = Gson().toJson(playLines) ?: "[]",
                lastUpdateTime = 0L,
                isUpdate = cartoon.isUpdate,
                tags = "",
                isShowLine = playLines !is DetailedComponent.NonPlayLine,
                sourceName = sourceName
            )
        }
    }
}