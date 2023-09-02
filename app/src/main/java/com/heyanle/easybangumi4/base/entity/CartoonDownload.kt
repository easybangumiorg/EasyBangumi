package com.heyanle.easybangumi4.base.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
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

    val decodeType: Int = PlayerInfo.DECODE_TYPE_OTHER,
    val downloadUri: String = "",

    val sourceName: String,

    val playLineLabel: String,

    val episodeLabel: String,

    val taskId: Long = 0L,

    val status: Int = 0, // 0->Ariaing 2->Decrypting 3->Transcoding 4-> Completely -1->Error
    val errorMsg: String = "", // 错误信息

    val createTime: Long,
) {

    companion object {
        fun fromCartoonInfo(
            cartoonInfo: CartoonInfo,
            downloadId: Long,
            playLineLabel: String,
            episodeLabel: String,
            playerInfo: PlayerInfo,
            createTime: Long = System.currentTimeMillis()
        ): CartoonDownload {
            return CartoonDownload(
                downloadId = 0,
                id = cartoonInfo.id,
                cartoonInfo.url,
                cartoonInfo.source,
                cartoonInfo.title,
                cartoonInfo.coverUrl,
                playerInfo.decodeType,
                playerInfo.uri,
                cartoonInfo.sourceName,
                playLineLabel,
                episodeLabel,
                taskId = downloadId,
                createTime = createTime,
            )
        }
    }

    fun toCartoonIdentify(): String {
        return "${id},${source},${URLEncoder.encode(url, "utf-8")}"
    }

    fun toIdentify(): String {
        return "${toCartoonIdentify()}-${playLineLabel},${episodeLabel}"
    }

}