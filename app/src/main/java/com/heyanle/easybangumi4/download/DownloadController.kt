package com.heyanle.easybangumi4.download

import android.app.Application
import android.net.Uri
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
import com.heyanle.easybangumi4.base.db.dao.CartoonDownloadDao
import com.heyanle.easybangumi4.base.entity.CartoonDownload
import com.heyanle.easybangumi4.base.entity.CartoonInfo
import com.heyanle.easybangumi4.exo.HeaderDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/8/13 22:18.
 * https://github.com/heyanLE
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class DownloadController(
    private val application: Application,
    private val cartoonDownloadDao: CartoonDownloadDao,
    private val manager: DownloadManager,
    private val headerDataSourceFactory: HeaderDataSourceFactory
) : DownloadManager.Listener {

    private val scope = MainScope()

    private val _downloadFlow = MutableStateFlow(emptyList<Download>())
    val downloadFLow = _downloadFlow.asStateFlow()


    init {
        manager.addListener(this)
    }

    fun getById(id: String) = manager.downloadIndex.getDownload(id)

    private fun getDownloads(@Download.State vararg states: Int) =
        manager.downloadIndex.getDownloads(*states)

    fun getCompleted() = getDownloads(Download.STATE_COMPLETED)

    fun getAllDownloads() = getDownloads(
        Download.STATE_QUEUED,
        Download.STATE_STOPPED,
        Download.STATE_DOWNLOADING,
        Download.STATE_COMPLETED,
        Download.STATE_FAILED,
        Download.STATE_REMOVING,
        Download.STATE_RESTARTING
    )

    fun removeDownloads(ids: List<String>) {
        ids.forEach { manager.removeDownload(it) }
    }

    fun removeDownload(id: String) {
        manager.removeDownload(id)
    }


    fun download(
        cartoonInfo: CartoonInfo,
        playLine: PlayLine,
        playLineIndex: Int,
        episode: Int,
        playerInfo: PlayerInfo,
    ) {


        playerInfo.header?.let {
            if (it.isNotEmpty()) {
                headerDataSourceFactory.put(playerInfo.uri, it)
            }
        }

        val mimeType = when (playerInfo.decodeType) {
            PlayerInfo.DECODE_TYPE_HLS -> {
                MimeTypes.APPLICATION_M3U8
            }

            PlayerInfo.DECODE_TYPE_DASH -> {
                MimeTypes.APPLICATION_MPD
            }

            else -> {
                MimeTypes.APPLICATION_MP4
            }
        }
        val downloadRequest = DownloadRequest.Builder(
            cartoonInfo.toIdentify() + "${playLineIndex}/${episode}",
            Uri.parse(playerInfo.uri)
        ).setMimeType(mimeType).build()

        DownloadService.sendAddDownload(
            application,
            MediaDownloadService::class.java,
            downloadRequest,
            /* foreground= */ false
        )
        scope.launch(Dispatchers.IO) {
            val download = CartoonDownload.fromCartoonInfo(
                cartoonInfo,
                cartoonInfo.toIdentify() + "${playLineIndex}/${episode}",
                playLine.label,
                playLineIndex,
                playLine.episode.getOrNull(episode) ?: "",
                episode
            )
            cartoonDownloadDao.deleteWithDownloadId(download.downloadId)
            cartoonDownloadDao.insert(download)
        }

    }

    override fun onInitialized(downloadManager: DownloadManager) {
        super.onInitialized(downloadManager)
        val list = arrayListOf<Download>()
        val c = getAllDownloads()
        c.use {
            for (i in 0 until c.count) {
                c.moveToPosition(i)
                list.add(c.download)
            }
        }
        _downloadFlow.update {
            list
        }
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: Exception?
    ) {
        super.onDownloadChanged(downloadManager, download, finalException)
        _downloadFlow.update {
            it.map {
                if (it.request.id == download.request.id) {
                    download
                } else {
                    it
                }
            }
        }
    }

    override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
        super.onDownloadRemoved(downloadManager, download)
        _downloadFlow.update {
            it.filter {
                it.request.id != download.request.id
            }
        }
        scope.launch(Dispatchers.IO) {
            cartoonDownloadDao.deleteWithDownloadId(download.request.id)
        }
    }


}