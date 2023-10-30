package com.heyanle.easybangumi4.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.MainActivity
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * Created by HeYanLe on 2023/10/4 22:43.
 * https://github.com/heyanLE
 */
class DownloadService : Service() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "DOWNLOAD_CHANNEL_ID"
        private const val NOTIFICATION_CHANNEL_NAME = "下载通知"
        private const val FOREGROUND_ID = 137173

        const val TAG = "DownloadService"

        fun tryStart() {
            "tryStart".logi(TAG)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                APP.startForegroundService(Intent(APP, DownloadService::class.java))
            } else {
                APP.startService(Intent(APP, DownloadService::class.java))
            }
        }
    }

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private var notificationChannel: NotificationChannel? = null

    private val scope = MainScope()
    private val downloadController: DownloadController by Injekt.injectLazy()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        "onStartCommand".logi(TAG)
        val notification = getNotification(downloadController.downloadItem.value ?: emptyList())
        if (notification == null) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }
        startForeground(FOREGROUND_ID, notification)
        scope.launch {
            downloadController.downloadItem.collectLatest {
                val n = getNotification(it ?: emptyList())
                if (n == null) {
                    stopSelf()
                    notificationManager.cancel(FOREGROUND_ID)
                } else {
                    notificationManager.notify(FOREGROUND_ID, n)
                }
            }
        }
        return START_STICKY
    }

    private fun getNotification(downloadItem: List<DownloadItem>): Notification? {
        val process = downloadItem.count { it.state == 0 || it.state == 1 || it.state == 2 }
        if (process == 0) {
            return null
        }
        "getNotification ${process}".logi(TAG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel?.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            //notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationChannel?.let {
                notificationManager.createNotificationChannel(it)
            }
        }
        val i =
            Intent(applicationContext, MainActivity::class.java) //点击通知栏后想要被打开的页面MainActivity.class

        val pendingIntent =
            PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE) //点击通知栏触发跳转

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.app_logo)
            .setContentTitle(stringRes(com.heyanle.easy_i18n.R.string.downloading))
            .setContentText(stringRes(com.heyanle.easy_i18n.R.string.x_downloading, process))
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

}