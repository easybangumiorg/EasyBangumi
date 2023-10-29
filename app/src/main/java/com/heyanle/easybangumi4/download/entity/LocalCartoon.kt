package com.heyanle.easybangumi4.download.entity

import androidx.room.Ignore
import com.heyanle.easybangumi4.base.utils.getMatchReg
import java.io.File

/**
 * Created by HeYanLe on 2023/9/17 15:41.
 * https://github.com/heyanLE
 */
data class LocalCartoon (

    // uuid
    val uuid: String,

    // cartoon 外键
    val cartoonId: String,
    val cartoonUrl: String,
    val cartoonSource: String,

    // 展示数据
    val sourceLabel: String,
    val cartoonTitle: String,
    val cartoonCover: String,
    val cartoonDescription: String,
    val cartoonGenre: String,

    var playLines: ArrayList<LocalPlayLine> = arrayListOf(),
){

    fun clearDirty(){
        val removeLine = mutableSetOf<LocalPlayLine>()
        playLines.forEach {
            val removeEpisode = mutableSetOf<LocalEpisode>()
            it.list.forEach {
                val f = File(it.path)
                if(!f.exists()){
                    removeEpisode.add(it)
                }
            }
            it.list.removeAll(removeEpisode)
            if(it.list.isEmpty()){
                removeLine.add(it)
            }
        }
        playLines.removeAll(removeLine)
    }

    fun match(query: String): Boolean{
        var matched = false
        for (match in query.split(',')) {
            val regex = match.getMatchReg()
            if (cartoonTitle.matches(regex)) {
                matched = true
                break
            }
        }
        return matched
    }

    @Ignore
    private var genres: List<String>? = null

    fun getGenres(): List<String>? {
        if (cartoonGenre.isEmpty()) {
            return null
        }
        if (genres == null) {
            genres = cartoonGenre.split(",").map { it.trim() }.filterNot { it.isBlank() }.distinct()
        }
        return genres
    }

}

data class LocalPlayLine(
    val id: String,
    val label: String,
    val list: ArrayList<LocalEpisode> = arrayListOf(),
)

data class LocalEpisode(
    val order: Int,
    val label: String,
    val path: String,
)