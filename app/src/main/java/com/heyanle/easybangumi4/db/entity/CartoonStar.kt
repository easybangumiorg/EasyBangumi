package com.heyanle.easybangumi4.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.bangumi_source_api.api.entity.CartoonImpl
import com.heyanle.bangumi_source_api.api.entity.PlayLine

/**
 * Created by HeYanLe on 2023/3/4 14:26.
 * https://github.com/heyanLE
 */
@Entity
data class CartoonStar(

    @PrimaryKey(autoGenerate = true)
    var starId: Int = 0,

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

    var createTime: Long = System.currentTimeMillis(),

    var playLineString: String,

    var isInitializer: Boolean = false,

    var lastUpdateTime: Long = 0L,
) {

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
                playLineString = Gson().toJson(playLines),
                isInitializer = true,
                lastUpdateTime = 0L,
                isUpdate = cartoon.isUpdate
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
        )
    }

    fun toCartoon(): Cartoon? {
        return if(isInitializer) CartoonImpl(
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


}