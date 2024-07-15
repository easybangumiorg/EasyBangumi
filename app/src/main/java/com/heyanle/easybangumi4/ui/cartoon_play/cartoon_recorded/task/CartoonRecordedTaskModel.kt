package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.task

import android.content.ContentValues
import android.content.Context
import android.graphics.RectF
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.exo.recorded.task.AbsRecordedTask
import com.heyanle.easybangumi4.exo.recorded.task.GifRecordedTask
import com.heyanle.easybangumi4.exo.recorded.task.Mp4RecordedTask
import com.heyanle.easybangumi4.exo.recorded.task.RecordedTask
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.MediaAndroidUtils
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanle on 2024/6/23.
 * https://github.com/heyanLE
 */
@OptIn(UnstableApi::class)
class CartoonRecordedTaskModel(
    val ctx: Context,
    val playerInfo: PlayerInfo,
    val cartoonMediaSourceFactory: CartoonMediaSourceFactory,
    val start: Long,
    val end: Long,
    val cropRect: RectF,
    // 1 -> gif 2 -> mp4
    val type: Int,
    val speed: Float = 1f,
    val onDismissRequest: () -> Unit,
) : AbsRecordedTask.Listener {
    val scope = MainScope()
    private val outputCacheFolder = File(ctx.getCachePath("Recorded"))
        .apply {
            deleteRecursively()
            mkdirs()
        }


    var task: RecordedTask? = null

    var process by mutableIntStateOf(-2)
    var status by mutableStateOf<String?>(null)

    var fps = mutableIntStateOf(if (type == 1) 15 else 30)
    var quality = mutableIntStateOf(if (type == 1) 30 else 100)


    fun start() {
        if (task != null) {
            return
        }
        val clippingConfiguration = MediaItem.ClippingConfiguration.Builder()
            .setStartPositionMs(start)
            .setEndPositionMs(end)
            .setStartsAtKeyFrame(false)
            .build()
        process = 0
        if (type == 2) {
            task = Mp4RecordedTask(
                ctx,
                cartoonMediaSourceFactory.getMediaItem(playerInfo),
                cartoonMediaSourceFactory.getClipMediaSourceFactory(
                    playerInfo,
                    clippingConfiguration
                ),
                outputCacheFolder,
                "recorded_${System.currentTimeMillis()}.mp4",
                start,
                end,
                Crop(
                    cropRect.left - 0.5f,
                    cropRect.right - 0.5f,

                    0.5f - cropRect.bottom,
                    0.5f - cropRect.top,
                ),
                fps.value,
                quality.value,
                speed,
            ).apply {
                listener = this@CartoonRecordedTaskModel
                start()
            }
        } else {
            task = GifRecordedTask(
                ctx,
                cartoonMediaSourceFactory.getMediaItem(playerInfo),
                cartoonMediaSourceFactory.getMediaSourceFactory(playerInfo),
                outputCacheFolder,
                "recorded_${System.currentTimeMillis()}.gif",
                start,
                end,
                cropRect,
                fps.value,
                quality.value,
            ).apply {
                listener = this@CartoonRecordedTaskModel
                start()
            }
        }
    }

    override fun onProcess(process: Int, label: String?) {
        this.process = process
        this.status = label
    }

    override fun onError(e: Exception, errorMsg: String?) {
        errorMsg ?: e.message?.let {
            logi(it)
            it.moeSnackBar()
        }
        onDismissRequest()
    }

    override fun onCompletely(file: File) {
        scope.launch {
            MediaAndroidUtils.saveToDownload(file, "recorded")
            stringRes(com.heyanle.easy_i18n.R.string.record_completely).toast()
            onDismissRequest()
        }


    }

    fun stop() {
        task?.stop()
    }
}