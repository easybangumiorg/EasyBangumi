package com.heyanle.easybangumi4.download

import android.app.Notification
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import com.heyanle.easybangumi4.R
import com.heyanle.injekt.core.Injekt



/**
 * Created by HeYanLe on 2023/8/13 15:25.
 * https://github.com/heyanLE
 */

private const val DOWNLOAD_CHANNEL_ID = "download_channel"
private const val FOREGROUND_NOTIFICATION_ID = 77665
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MediaDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_CHANNEL_ID,
    com.heyanle.easy_i18n.R.string.channel_name,
    0
) {

    private val innerDownloadManager: DownloadManager by Injekt.injectLazy()

    override fun getDownloadManager(): DownloadManager {
        return innerDownloadManager
    }

    override fun getScheduler(): Scheduler {
        return PlatformScheduler(this, 1)
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        return DownloadNotificationHelper(this, DOWNLOAD_CHANNEL_ID).buildProgressNotification(
            this,
            R.drawable.baseline_download_24,
            null,
            null,
            downloads,
            notMetRequirements
        )
    }
}