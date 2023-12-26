package com.heyanle.easybangumi4.download

import android.content.Context
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.download.entity.LocalCartoon
import com.heyanle.easybangumi4.download.entity.LocalEpisode
import com.heyanle.easybangumi4.download.entity.LocalPlayLine
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanlin on 2023/10/2.
 */
class LocalCartoonController(
    private val context: Context,
) {
    private val scope = MainScope()
    private val rootFolder = File(context.getFilePath("download"))
    private val localCartoonJson = File(rootFolder, "local.json")
    private val localCartoonJsonTem = File(rootFolder, "local.json.bk")

    private val _localCartoon = MutableStateFlow<List<LocalCartoon>?>(null)
    val localCartoon = _localCartoon.asStateFlow()

    init {

        scope.launch(Dispatchers.IO) {
            if(!localCartoonJson.exists() && localCartoonJsonTem.exists()){
                localCartoonJsonTem.renameTo(localCartoonJson)
            }
            if(localCartoonJson.exists()){
                runCatching {
                    val json = localCartoonJson.readText()
                    val d = json.jsonTo<List<LocalCartoon>>()?: emptyList()
                    d.forEach {
                        it.clearDirty()
                    }
                    _localCartoon.update {
                        d
                    }
                }.onFailure {
                    it.printStackTrace()
                    localCartoonJson.delete()
                    localCartoonJsonTem.delete()
                    _localCartoon.update {
                        emptyList()
                    }
                }
            }
        }
    }

    fun onComplete(downloadItem: DownloadItem){
        scope.launch(Dispatchers.IO) {
            val function: (List<LocalCartoon>?) -> List<LocalCartoon> = {
                val d = it?.toMutableList() ?: mutableListOf()
                val old = d.find {
                    it.cartoonId == downloadItem.cartoonId && it.cartoonSource == downloadItem.cartoonSource && it.cartoonUrl == downloadItem.cartoonUrl
                }
                val new = if (old == null) {
                    val newLocal = LocalCartoon(
                        uuid = downloadItem.uuid,
                        sourceLabel = downloadItem.sourceLabel,
                        cartoonId = downloadItem.cartoonId,
                        cartoonUrl = downloadItem.cartoonUrl,
                        cartoonSource = downloadItem.cartoonSource,
                        cartoonTitle = downloadItem.cartoonTitle,
                        cartoonGenre = downloadItem.cartoonGenre,
                        cartoonCover = downloadItem.cartoonCover,
                        cartoonDescription = downloadItem.cartoonDescription
                    )
                    d.add(newLocal)
                    newLocal
                } else {
                    old
                }
                val oldLines = new.playLines.find {
                    it.id == downloadItem.playLine.id && it.label == downloadItem.playLine.label
                }
                val newLines = if (oldLines == null) {
                    val newLine = LocalPlayLine(downloadItem.playLine.id, downloadItem.playLine.label)
                    new.playLines.add(newLine)
                    newLine
                } else {
                    oldLines
                }
                val newEpisode = LocalEpisode(
                    label = downloadItem.episodeLabel,
                    path = File(downloadItem.folder, downloadItem.fileNameWithoutSuffix+".mp4").absolutePath
                )
                newLines.list.add(newEpisode)
                d
            }
            while (true) {
                val prevValue = _localCartoon.value
                val nextValue = function(prevValue)
                if (_localCartoon.compareAndSet(prevValue, nextValue)) {
                    save(nextValue)
                    return@launch
                }
            }
        }
    }

    private fun save(value: List<LocalCartoon>){
        localCartoonJsonTem.delete()
        localCartoonJsonTem.createNewFile()
        localCartoonJsonTem.writeText(value.toJson())
        localCartoonJsonTem.renameTo(localCartoonJson)
    }

}