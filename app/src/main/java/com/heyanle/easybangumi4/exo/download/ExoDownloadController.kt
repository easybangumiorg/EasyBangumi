package com.heyanle.easybangumi4.exo.download

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.getCachePath
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */
@UnstableApi
class ExoDownloadController(
    private val context: Context,
    private val cartoonMediaSourceFactory: CartoonMediaSourceFactory,
    private val downloadCache: Cache,
    private val mediaDownloadCacheDB: MediaDownloadCacheDB,
    private val downloadPreference: CartoonDownloadPreference,
): DownloadManager.Listener {


    val dataSourceFactory = DefaultHttpDataSource.Factory()
    val downloadExecutor = Executors.newCachedThreadPool()
    val downloadManager =
        DownloadManager(
            context,
            mediaDownloadCacheDB,
            downloadCache,
            dataSourceFactory,
            downloadExecutor
        ).apply {
            maxParallelDownloads = downloadPreference.downloadMaxCountPref.get().toInt()
            resumeDownloads()
        }


    fun newDownloadTask(
        uuid: String,
        playerInfo: PlayerInfo,
        callback: (downloadRequest: DownloadRequest?, e: IOException?) -> Unit
    ){
        val downloadHelper =
            DownloadHelper.forMediaItem(
                context,
                cartoonMediaSourceFactory.getMediaItem(playerInfo),
                DefaultRenderersFactory(context),
                cartoonMediaSourceFactory.getDataSourceFactory(playerInfo),
            )
        downloadHelper.prepare(object: DownloadHelper.Callback {
            override fun onPrepared(helper: DownloadHelper) {
                val downloadRequest = helper.getDownloadRequest(uuid, ByteArray(1).apply {
                    this[0] = playerInfo.decodeType.toByte()
                })
                downloadManager.addDownload(downloadRequest)
                callback(downloadRequest, null)
            }

            override fun onPrepareError(helper: DownloadHelper, e: IOException) {
                e.printStackTrace()
                callback(null, e)
            }
        })

    }



}