package com.heyanle.easybangumi4.cartoon.story.download.service


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.heyanle.easybangumi4.MainActivity
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.utils.stringRes


/**
 * Created by heyanle on 2024/8/4.
 * https://github.com/heyanLE
 */
class DownloadingService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            var notification: Notification? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                //只在Android O之上需要渠道
                val notificationChannel = NotificationChannel(
                    "easybangumi",
                    "easybangumi_downloading",
                    NotificationManager.IMPORTANCE_HIGH
                )
                //如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道，通知才能正常弹出
                manager.createNotificationChannel(notificationChannel)
                notification = Notification.Builder(this, "easybangumi")
                    .setContentTitle(stringRes(com.heyanle.easy_i18n.R.string.app_name))
                    .setContentText(stringRes(com.heyanle.easy_i18n.R.string.downloading))
                    .setSmallIcon(R.mipmap.logo_new)
                    .setContentIntent(pendingIntent)
                    .setTicker(stringRes(com.heyanle.easy_i18n.R.string.downloading))
                    .build()
            }

            startForeground(1, notification)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        }
    }
}