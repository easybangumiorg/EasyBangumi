package com.heyanle.easybangumi4.base.entity

import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.bangumi_source_api.api.entity.CartoonImpl
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine

/**
 * Created by HeYanLe on 2023/3/4 14:26.
 * https://github.com/heyanLE
 */
@Entity(primaryKeys = ["id", "source", "url"])
data class CartoonStar(

    var id: String,              // 标识，由源自己支持，用于区分番剧

    var source: String,

    var url: String,

    var title: String,

    var genre: String,          // 标签，一般为 "xx, xx"

    var coverUrl: String,

    var intro: String,

    var description: String,

    var updateStrategy: Int,

    var isUpdate: Boolean,

    var status: Int,

    var watchProcess: String, // 播放进度 “1/11”，在追番页面展示

    var reversal: Boolean, // 是否反转集数

    var tags: String, // 番剧分类 "1, 2, 3" 的格式

    var createTime: Long = System.currentTimeMillis(),

    var playLineString: String,

    var isInitializer: Boolean = false,

    var lastUpdateTime: Long = 0L,
) {

    @Ignore
    private var genres: List<String>? = null

    fun getGenres(): List<String>? {
        if (genre.isEmpty()) {
            return null
        }
        if (genres == null) {
            genres = genre.split(", ").map { it.trim() }.filterNot { it.isBlank() }.distinct()
        }
        return genres
    }

    companion object {
        fun fromCartoon(cartoon: Cartoon, playLines: List<PlayLine>): CartoonStar {
            return CartoonStar(
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
                isInitializer = true,
                lastUpdateTime = 0L,
                isUpdate = cartoon.isUpdate,
                reversal = false,
                watchProcess = "",
                tags = "",
            )
        }
    }

    fun fromCartoon(cartoon: CartoonCover): CartoonStar {
        return CartoonStar(
            id = cartoon.id,
            source = cartoon.source,
            url = cartoon.url,
            title = cartoon.title,
            genre = "",
            coverUrl = cartoon.coverUrl ?: "",
            intro = cartoon.intro ?: "",
            description = "",
            updateStrategy = Cartoon.UPDATE_STRATEGY_NEVER,
            status = Cartoon.STATUS_UNKNOWN,
            playLineString = "",
            isInitializer = false,
            lastUpdateTime = 0L,
            isUpdate = false,
            reversal = false,
            watchProcess = "",
            tags = "",
        )
    }

    fun toCartoon(): Cartoon? {
        return if (isInitializer) CartoonImpl(
            id = this.id,
            source = this.source,
            url = this.url,
            title = this.title,
            genre = this.genre ?: "",
            coverUrl = this.coverUrl ?: "",
            intro = this.intro ?: "",
            description = this.description ?: "",
            updateStrategy = this.updateStrategy,
            status = this.status
        ) else null
    }

    fun getPlayLine(): List<PlayLine> {
        return kotlin.runCatching {
            Gson().fromJson<List<PlayLine>>(
                playLineString,
                object : TypeToken<List<PlayLine>>() {}.type
            )
        }.getOrElse {
            it.printStackTrace()
            emptyList()
        }
    }

    fun matches(query: String): Boolean {
        var matched = false
        for (match in query.split(',')) {
            val regex = getMatchReg(match)
            if (title.matches(regex)) {
                matched = true
                break
            }
        }
        return matched

    }

    private fun getMatchReg(query: String): Regex {
        return buildString {
            append("(.*)(")
            append(query.split("").joinToString(")(.*)("))
            append(")(.*)")
        }.toRegex(RegexOption.IGNORE_CASE)
    }


    fun getSummary(): CartoonSummary {
        return CartoonSummary(id, source, url)
    }

    fun getIdentify(): String {
        return "${id},${source},${url}"
    }

    fun match(identify: String): Boolean {
        return this.getIdentify() == identify
    }

    fun match(cartoon: Cartoon): Boolean {
        return this.id == cartoon.id && this.source == cartoon.source && this.url == cartoon.url
    }

    fun match(cartoon: CartoonCover): Boolean {
        return this.id == cartoon.id && this.source == cartoon.source && this.url == cartoon.url
    }


}