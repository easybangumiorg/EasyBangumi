package com.heyanle.easybangumi4.cartoon_download

import android.content.Context
import com.heyanle.easybangumi4.cartoon_download.entity.DownloadItem
import com.heyanle.easybangumi4.cartoon_download.entity.LocalCartoon
import com.heyanle.easybangumi4.cartoon_download.entity.LocalEpisode
import com.heyanle.easybangumi4.cartoon_download.entity.LocalPlayLine
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

/**
 * Created by heyanlin on 2023/10/2.
 */
class LocalCartoonController(
    private val context: Context,
) {

    private val dispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val rootFolder = File(context.getFilePath("download"))
    private val localCartoonJson = File(rootFolder, "local.json")
    private val localCartoonJsonTem = File(rootFolder, "local.json.bk")

    private val _localCartoon = MutableStateFlow<List<LocalCartoon>?>(null)
    val localCartoon = _localCartoon.asStateFlow()

    init {

        scope.launch {
            _localCartoon.collectLatest {
                it?.let(::save)
            }
        }

        scope.launch() {
            if (!localCartoonJson.exists() && localCartoonJsonTem.exists()) {
                localCartoonJsonTem.renameTo(localCartoonJson)
            }
            if (localCartoonJson.exists()) {
                runCatching {
                    val json = localCartoonJson.readText()
                    var d = json.jsonTo<List<LocalCartoon>>() ?: emptyList()
                    d = d.flatMap {
                        val n = it.clearDirty()
                        if(n.playLines.isEmpty()){
                            emptyList()
                        }else{
                            listOf(n)
                        }
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

//        scope.launch() {
//            _localCartoon.collect {
//                it?.let {
//                    save(it)
//                }
//            }
//        }
    }

    fun onComplete(downloadItem: DownloadItem) {
        scope.launch() {

            val function: (List<LocalCartoon>?) -> List<LocalCartoon> = {

                var append = false
                val res = (it?: listOf()).toMutableList().map {
                    it.append(downloadItem).also {
                        if(it != null){
                            append = true
                        }
                    }?:it
                }
                if(append) res  else res + LocalCartoon(
                    uuid = downloadItem.uuid,
                    sourceLabel = downloadItem.sourceLabel,
                    cartoonId = downloadItem.cartoonId,
                    cartoonUrl = downloadItem.cartoonUrl,
                    cartoonSource = downloadItem.cartoonSource,
                    cartoonTitle = downloadItem.cartoonTitle,
                    cartoonGenre = downloadItem.cartoonGenre,
                    cartoonCover = downloadItem.cartoonCover,
                    cartoonDescription = downloadItem.cartoonDescription,
                    playLines = listOf(
                        LocalPlayLine(downloadItem.playLine.id, downloadItem.playLine.label, listOf(
                            LocalEpisode(
                                order = downloadItem.episode.order,
                                label = downloadItem.episode.label,
                                path = File(
                                    downloadItem.folder,
                                    downloadItem.fileNameWithoutSuffix + ".mp4"
                                ).absolutePath
                            )
                        ))
                    )
                )
            }
            _localCartoon.update {
                function(it)
            }
        }
    }

    fun remove(localCartoon: LocalCartoon) {
        scope.launch() {
            localCartoon.playLines.forEach {
                it.list.forEach {
                    File(it.path).delete()
                }
            }
            _localCartoon.update {
                it?.flatMap {
                    val list = it.clearDirty()
                    if(list.playLines.isEmpty()){
                        emptyList()
                    }else{
                        listOf(list)
                    }
                }
            }
        }
    }

    fun removeEpisode(episode: List<LocalEpisode>) {
        scope.launch() {
            episode.forEach {
                File(it.path).delete()
            }
            _localCartoon.update {
                it?.flatMap {
                    val list = it.clearDirty()
                    if(list.playLines.isEmpty()){
                        emptyList()
                    }else{
                        listOf(list)
                    }
                }
            }
        }
    }

    private fun save(value: List<LocalCartoon>) {
        value.logi("LocalCartoonController")
        localCartoonJsonTem.delete()
        localCartoonJsonTem.createNewFile()
        localCartoonJson.delete()
        localCartoonJsonTem.writeText(value.toJson())
        localCartoonJsonTem.renameTo(localCartoonJson)
    }

}