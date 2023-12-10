package com.heyanle.easybangumi4.cartoon_download.entity

import androidx.room.Ignore
import com.heyanle.easybangumi4.utils.getMatchReg
import java.io.File

/**
 * Created by HeYanLe on 2023/9/17 15:41.
 * https://github.com/heyanLE
 */
data class LocalCartoon(

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

    var playLines: List<LocalPlayLine>,
) {

    fun clearDirty(): LocalCartoon {
        return copy(
            playLines = playLines.flatMap {
                val new = it.clearDirty()
                if (new.list.isEmpty()) {
                    emptyList()
                } else {
                    listOf(new)
                }
            }
        )
    }

    fun append(downloadItem: DownloadItem): LocalCartoon? {
        if (downloadItem.cartoonId != cartoonId || downloadItem.cartoonUrl != cartoonUrl || downloadItem.cartoonSource != cartoonSource) {
            return null
        }
        var isAppend = false
        val list = playLines.map { playLine ->
            playLine.append(downloadItem).also {
                if (it != null) {
                    isAppend = true
                }
            } ?: playLine
        }
        if (!isAppend) {
            return copy(
                playLines = list + LocalPlayLine(
                    downloadItem.playLine.id, downloadItem.playLine.label, listOf(
                        LocalEpisode(
                            order = downloadItem.episode.order,
                            label = downloadItem.episode.label,
                            path = File(
                                downloadItem.folder,
                                downloadItem.fileNameWithoutSuffix + ".mp4"
                            ).absolutePath
                        )
                    )
                )
            )
        } else {
            return copy(
                playLines = list
            )
        }
    }

    fun match(query: String): Boolean {
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
    val list: List<LocalEpisode> = listOf(),
) {
    fun clearDirty(): LocalPlayLine {
        return copy(
            list = list.flatMap {
                val file = File(it.path)
                if (file.exists()) {
                    listOf(it)
                } else {
                    emptyList()
                }
            }
        )
    }

    fun append(downloadItem: DownloadItem): LocalPlayLine? {
        if (downloadItem.playLine.id != id || downloadItem.playLine.label != label) {
            return null
        }
        return copy(
            list = list + LocalEpisode(
                order = downloadItem.episode.order,
                label = downloadItem.episode.label,
                path = File(
                    downloadItem.folder,
                    downloadItem.fileNameWithoutSuffix + ".mp4"
                ).absolutePath
            )
        )
    }
}

data class LocalEpisode(
    val order: Int,
    val label: String,
    val path: String,
)