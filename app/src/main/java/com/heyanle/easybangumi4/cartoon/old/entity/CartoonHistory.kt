package com.heyanle.easybangumi4.cartoon.old.entity

import androidx.room.Entity
import com.heyanle.easybangumi4.utils.getMatchReg
import java.net.URLEncoder

/**
 * Created by HeYanLe on 2023/3/7 14:55.
 * https://github.com/heyanLE
 */
@Entity(primaryKeys = ["id", "source", "url"])
data class CartoonHistory(

    val id: String,
    val url: String,
    val source: String,

    val name: String,
    val cover: String,
    val intro: String,

    val lastLineId: String,
    val lastLinesIndex: Int,
    val lastLineTitle: String,

    val lastEpisodeId: String,
    val lastEpisodeOrder: Int,
    val lastEpisodeIndex: Int,
    val lastEpisodeTitle: String,

    val lastProcessTime: Long,
    val createTime: Long,
){
    fun matches(query: String): Boolean{
        var matched = false
        for(match in query.split(',')){
            val regex = match.getMatchReg()
            if(name.matches(regex)){
                matched = true
                break
            }
        }
        return matched

    }

    fun toIdentify(): String {
        return "${id},${source},${URLEncoder.encode(url, "utf-8")}"
    }

}