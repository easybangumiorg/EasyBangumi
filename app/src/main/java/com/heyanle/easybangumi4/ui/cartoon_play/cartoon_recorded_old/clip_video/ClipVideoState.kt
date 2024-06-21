package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded_old.clip_video

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.MediaSource
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.exo.thumbnail.OutputThumbnailHelper
import com.heyanle.easybangumi4.exo.thumbnail.ThumbnailBuffer
import com.heyanle.easybangumi4.utils.dip2px
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.TreeMap

/**
 * Created by heyanle on 2024/6/16.
 * https://github.com/heyanLE
 */
@UnstableApi
class ClipVideoState(
    val ctx: Context,
    val mediaSource: MediaSource,
    val scope: CoroutineScope,
    val thumbnailBuffer: ThumbnailBuffer,
    start: Long,
    end: Long,
    defaultPosition: Long,
)  {

    companion object {
        private const val TAG = "ClipVideoState"
    }


    var start by mutableLongStateOf(start)
    var end by mutableLongStateOf(end)
    var positionDuring = end - start

    var selectionStart by mutableLongStateOf(start)
    var selectionEnd by mutableLongStateOf(end)
    var selectionDuring = selectionEnd - selectionStart

    var currentPosition by mutableLongStateOf(defaultPosition)

    var horizontalPadding = 18.dip2px()
    var verticalPadding = 6.dip2px()

    var width by mutableStateOf(0)
    var height by mutableStateOf(0)

    var videoSize by mutableStateOf(VideoSize(1920, 1080))

    var jpgPositionList by mutableStateOf(listOf<Long>())
    var bmpTreeMap by mutableStateOf(TreeMap<Long, File>())

    // 0: normal, 1: select start, 2: select end, 3: select position
    var focusMode by mutableStateOf(0)

    // 待定系数法，这里使用一次函数将视频轴 [start, end] 部分与 View 的可滑动部分 [horizontalPadding, width - horizontalPadding] 对应
    // 这里分别计算，以减少误差

    // horizontalPadding = start * k + b
    // width - horizontalPadding = end * k + b

    @Composable
    fun getPos2PxParam(): Pair<Float, Float>{
        return remember(width, end, start, horizontalPadding) {
            val k = (width - horizontalPadding) / (end - start).toFloat()
            val b = horizontalPadding - start * k
            k to b
        }
    }

    // start = horizontalPadding * k + b
    // end = (width - horizontalPadding) * k + b
    @Composable
    fun getPx2PosParam(): Pair<Float, Float> {
        return remember(width, end, start, horizontalPadding) {
            val k = (end - start) / (width - 2 * horizontalPadding).toFloat()
            val b = start - horizontalPadding * k
            k to b
        }
    }



    init {
        thumbnailBuffer.onTreeMapChange = {
            bmpTreeMap = it
        }
        thumbnailBuffer.dispatchCurrent()
    }

    val outputThumbnailHelper = OutputThumbnailHelper(
        ctx,
        mediaSource,
        File(APP.getCachePath("thumbnail")),
        thumbnailBuffer,
        start,
        end,
        5000
    ).apply {
        this.onVideoSizeChange = {
            if (it.width > 0 && it.height > 0) {
                scope.launch {
                    "onVideoSizeChange ${it.width} ${it.height}".logi(TAG)
                    this@ClipVideoState.videoSize = it
                }

            }
        }
    }

    fun onViseChange(width: Int, height: Int){
        "onViewChange".logi(TAG)
        this.width = width
        this.height = height
    }

    fun onLaunch(){
        "onLaunch".logi(TAG)
        outputThumbnailHelper.start()
    }

    fun onDispose(){
        "onDispose".logi(TAG)
        outputThumbnailHelper.release()
    }


    fun check(): Boolean {

        if (end <= start || end <= 0 || start < 0){
            "check false1".logi(TAG)
            return false
        }

        if (width == 0 || height == 0){
            "check false2".logi(TAG)
            return false
        }

        if (videoSize.width == 0 || videoSize.height == 0){
            "check false3".logi(TAG)
            return false
        }
        if (selectionStart < start){
            selectionStart = start
        }
        if (selectionEnd > end){
            selectionEnd = end
        }
        if (currentPosition !in selectionStart..selectionEnd){
            currentPosition = selectionStart
        }
        "check true".logi(TAG)
        return true

    }



    fun getShowTime(): String {
        val showTimePosition = when(focusMode){
            1 -> selectionStart
            2 -> selectionEnd
            else -> currentPosition
        }
        val showTimeSec = showTimePosition/1000
        return if (showTimeSec > 3600){
            "%02d:%02d:%02d".format(Locale.getDefault(), showTimeSec/3600, showTimeSec%3600/60, showTimeSec%60)
        }else{
            "%02d:%02d".format(Locale.getDefault(), showTimeSec/60, showTimeSec%60)
        }

    }

}