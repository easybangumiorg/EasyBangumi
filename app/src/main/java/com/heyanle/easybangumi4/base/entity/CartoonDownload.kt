package com.heyanle.easybangumi4.base.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URLEncoder

/**
 * 下载任务
 * Created by HeYanLe on 2023/8/13 22:29.
 * https://github.com/heyanLE
 */
@Entity()
data class CartoonDownload(

    @PrimaryKey(autoGenerate = true)
    val downloadId: Int = 0,
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

    val taskId: String = "",
    val tsPath: String = "",
    val path: String = "",
    val status: Int = 0, // 0->Init 2->Downloading 3->Converting 4-> Completely

    val playerInfoString: String = "", // PlayerInfo

    val process: Float = 0f, // 仅作为重启后首次展示的进度， -1 为当前任务状态不支持展示进度

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
                0,
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
                createTime = System.currentTimeMillis(),
            )
        }
    }

    fun toIdentify(): String {
        return "${id},${source},${URLEncoder.encode(url, "utf-8")},${playLineIndex}-${episodeIndex}"
    }

}