package com.heyanle.easybangumi4.base.entity

import androidx.room.Entity
import java.net.URLEncoder

/**
 * 下载任务
 * Created by HeYanLe on 2023/8/13 22:29.
 * https://github.com/heyanLE
 */
@Entity(primaryKeys = ["downloadId"])
data class CartoonDownload(

    val id: String,
    val url: String,
    val source: String,

    val name: String,
    val cover: String,

    val sourceName: String,

    val playLineIndex: Int,
    val playLineString: String,

    val episodeIndex: Int,
    val episodeLabel: String,

    val downloadId: String,

    val createTime: Long,
) {

    companion object {
        fun fromCartoonInfo(
            cartoonInfo: CartoonInfo,
            downloadId: String,
            playLineLabel: String,
            playLineIndex: Int,
            episodeLabel: String,
            episodeIndex: Int
        ): CartoonDownload {
            return CartoonDownload(
                cartoonInfo.id,
                cartoonInfo.url,
                cartoonInfo.source,
                cartoonInfo.title,
                cartoonInfo.coverUrl,
                cartoonInfo.sourceName,

                playLineIndex,
                playLineLabel,
                episodeIndex,
                episodeLabel,
                downloadId,
                System.currentTimeMillis()
            )
        }
    }

    fun toIdentify(): String {
        return "${id},${source},${URLEncoder.encode(url, "utf-8")}"
    }

}