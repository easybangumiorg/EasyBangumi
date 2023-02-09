package com.heyanle.easybangumi.ui.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.MainActivity
import com.heyanle.easybangumi.NAV
import com.heyanle.easybangumi.PLAY
import java.net.URLEncoder


/**
 * 获取当前播放信息，重播，尝试连播
 * 提供给播放器组件调用
 * Created by HeYanLe on 2023/2/5 12:43.
 * https://github.com/heyanLE
 */
object AnimPlayingManager {


    private fun getCurInfo(): BangumiInfoState.Info? {
        return BangumiPlayManager.getCurAnimPlayItemController()?.infoState?.value as? BangumiInfoState.Info
    }

    private fun getCurPlay(): AnimPlayState.Play? {
        return BangumiPlayManager.getCurAnimPlayItemController()?.playerState?.value as? AnimPlayState.Play
    }


    private fun checkPlay(line: Int, epi: Int): Boolean {
        val status = getCurInfo()
            ?: return false
        val curPlayMsg = status.playMsg
        if (curPlayMsg.isEmpty()) {
            return false
        }
        val lines = curPlayMsg.keys.toList()
        if (line < 0 || line >= lines.size) {
            return false
        }
        val key = lines[line]
        val es = curPlayMsg[key] ?: return false
        if (epi < 0 || epi >= es.size) {
            return false
        }
        return true
    }

    fun getCurTitle(): String {
        val status =
            getCurInfo() ?: return ""
        val curPlay = getCurPlay() ?: return ""
        val curPlayMsg = status.playMsg
        val curLine = curPlay.lineIndex
        val curEp = curPlay.episode
        if (curPlayMsg.isEmpty()) {
            return ""
        }
        val lines = curPlayMsg.keys.toList()
        if (curLine < 0 || curLine >= lines.size) {
            return ""
        }
        val key = lines[curLine]
        val eps = curPlayMsg[key] ?: return ""
        if (curEp < 0 || curEp >= eps.size) {
            return ""
        }
        return eps[curEp]
    }

    fun getCurPlayList(): List<String> {

        val res = arrayListOf<String>()
        val status = getCurInfo()
            ?: return res
        val curPlay = getCurPlay() ?: return res
        val curPlayMsg = status.playMsg
        val curLine = curPlay.lineIndex
        if (curPlayMsg.isEmpty()) {
            return res
        }
        val lines = curPlayMsg.keys.toList()
        if (curLine < 0 || curLine >= lines.size) {
            return res
        }
        curPlayMsg[lines[curLine]]?.forEach {
            res.add(it)
        }
        return res
    }

    fun replay(): Boolean {
        val curPlay = getCurPlay() ?: return false
        val line = curPlay.lineIndex
        val epi = curPlay.episode
        if (checkPlay(line, epi)) {
            BangumiPlayManager.getCurAnimPlayItemController()?.loadPlay(line, epi)
            return true
        }
        return false
    }

    fun tryNext(): Boolean {
        val curPlay = getCurPlay() ?: return false
        val line = curPlay.lineIndex
        val epi = curPlay.episode + 1
        if (checkPlay(line, epi)) {
            BangumiPlayManager.getCurAnimPlayItemController()?.loadPlay(line, epi)
            return true
        }
        return false
    }

    fun tryChangeEpisode(episode: Int): Boolean {
        val curPlay = getCurPlay() ?: return false
        val line = curPlay.lineIndex
        if (checkPlay(line, episode)) {
            BangumiPlayManager.getCurAnimPlayItemController()?.loadPlay(line, episode)
            return true
        }
        return false
    }

    var pendingIntent: PendingIntent? = null

    fun getCurPendingIntent(): PendingIntent {
        val pd = pendingIntent
        return if (pd != null) {
            pd
        } else {
            val intent = Intent(BangumiApp.INSTANCE, MainActivity::class.java)
            val flagImmutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
            PendingIntent.getActivity(BangumiApp.INSTANCE, 0, intent, flagImmutable)
        }
    }

    fun newBangumi(id: String, source: String, url: String) {
        val del = URLEncoder.encode(url, "utf-8")
        val idl = URLEncoder.encode(id, "utf-8")

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "$NAV://$PLAY/${source}/${del}?id=${idl}".toUri(),
            BangumiApp.INSTANCE,
            MainActivity::class.java
        )

        pendingIntent = TaskStackBuilder.create(BangumiApp.INSTANCE).run {
            addNextIntentWithParentStack(deepLinkIntent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
    }

}