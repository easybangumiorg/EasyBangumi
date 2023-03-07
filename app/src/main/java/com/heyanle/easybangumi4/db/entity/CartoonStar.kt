package com.heyanle.easybangumi4.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonImpl

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

    var isUpdate: Boolean,       // 是否更新，在追番页显示

    var status: Int,

    var createTime: Long = System.currentTimeMillis(),
) {

    companion object {
        fun fromCartoon(cartoon: Cartoon): CartoonStar{
            return CartoonStar(
                id = cartoon.id,
                source = cartoon.source,
                url = cartoon.url,
                title = cartoon.title,
                genre = cartoon.genre?:"",
                coverUrl = cartoon.coverUrl?:"",
                intro = cartoon.intro?:"",
                description = cartoon.description?:"",
                updateStrategy = cartoon.updateStrategy,
                isUpdate = cartoon.isUpdate,
                status = cartoon.status,
            )
        }
    }

    fun toCartoon():Cartoon {
        return CartoonImpl(
            id = this.id,
            source = this.source,
            url = this.url,
            title = this.title,
            genre = this.genre?:"",
            coverUrl = this.coverUrl?:"",
            intro = this.intro?:"",
            description = this.description?:"",
            updateStrategy = this.updateStrategy,
            isUpdate = this.isUpdate,
            status = this.status
        )
    }



}