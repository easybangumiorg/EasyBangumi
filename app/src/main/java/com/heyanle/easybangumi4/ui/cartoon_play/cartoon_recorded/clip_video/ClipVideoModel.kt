package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.exo.thumbnail.OutputThumbnailHelper
import com.heyanle.easybangumi4.exo.thumbnail.ThumbnailBuffer
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.dip2px
import com.heyanle.easybangumi4.utils.getCachePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.TreeMap

/**
 * Created by heyanlin on 2024/6/21.
 */
@UnstableApi
class ClipVideoModel(
    val ctx: Context,
    val exoPlayer: ExoPlayer,
    val playerInfo: PlayerInfo,
    val cartoonMediaSourceFactory: CartoonMediaSourceFactory,
    val scope: CoroutineScope,

    val thumbnailBuffer: ThumbnailBuffer,

    val start: Long,
    val end: Long,

    val currentPosition: Long,
) {

    companion object {
        const val horizontalPaddingDp = 18
        const val verticalPaddingDp = 6
        const val seekBarHeightDp = 60
    }

    val horizontalPaddingPx = dip2px(ctx, horizontalPaddingDp.toFloat())
    val verticalPaddingPx = dip2px(ctx, verticalPaddingDp.toFloat())
    val seekBarHeightPx = dip2px(ctx, seekBarHeightDp.toFloat())

    // 剪辑配置
    var selectionCurrent by mutableLongStateOf(start)
    var selectionStart by mutableLongStateOf(start)
    var selectionEnd by mutableLongStateOf(end)

    fun check(): Boolean {
        if (selectionEnd < selectionStart){
            selectionEnd = selectionStart
            return false
        }
        if (selectionCurrent < selectionStart){
            selectionCurrent = selectionStart
            return false
        }
        if (selectionCurrent > selectionEnd){
            selectionCurrent = selectionEnd
            return false
        }
        return selectionEnd >= selectionStart && selectionCurrent >= selectionStart && selectionCurrent in selectionStart..selectionEnd
    }

    // 运行时一些参数

    var jpgPositionList by mutableStateOf<List<Long>>(listOf())
    var bmpTreeMap by mutableStateOf(TreeMap<Long, File>())
    var pos2Px by mutableStateOf(0f to 0f)
    var px2Pos by mutableStateOf(0f to 0f)

    // 0: normal, 1: select start, 2: select end, 3: select position
    var focusMode: Int by mutableStateOf(0)

    // 加载缩略图
    val outputThumbnailHelper = OutputThumbnailHelper(
        ctx,
        cartoonMediaSourceFactory.getWithCache(playerInfo),
        File(APP.getCachePath("thumbnail")),
        thumbnailBuffer,
        start,
        end,
        2000
    )

    init {

        scope.launch {
            outputThumbnailHelper.start()
        }

        thumbnailBuffer.onTreeMapChange = { map ->
            bmpTreeMap = map
        }
        thumbnailBuffer.dispatchCurrent()
    }


    fun onSeekBarSizeChange(width: Int, height: Int){
        // 待定系数计算
        val k = (width - 2* horizontalPaddingPx) / (end - start).toFloat()
        val b = horizontalPaddingPx - start * k
        val pos2Px = k to b

        val k2 = (end - start) / (width - 2 * horizontalPaddingPx).toFloat()
        val b2 = start - horizontalPaddingPx * k2
        val px2Pos = k2 to b2

        // 缩略图 position 计算
        val pngWidth =height - 2*verticalPaddingPx
        var currentPx = horizontalPaddingPx
        val jpgPositionList = arrayListOf<Long>()
        while(currentPx <= width - horizontalPaddingPx){
            val pos = k2 * currentPx + b2
            jpgPositionList.add(pos.toLong())
            currentPx += pngWidth
        }

        //jpgPositionList.joinToString(", ").logi("ClipVideoModel")

        this@ClipVideoModel.pos2Px = pos2Px
        this@ClipVideoModel.px2Pos = px2Pos
        this@ClipVideoModel.jpgPositionList = jpgPositionList
    }

    fun onFocusChange(focus: Int){
        //"onFocusChange ${focus}".logi("ClipVideoModel")
        focusMode = focus
        if (focus > 0)
            exoPlayer.playWhenReady = false
    }

    fun onSelectionChange(pos: Float, focusMode: Int){
        //"onSelectionChange ${pos} ${focusMode}".logi("ClipVideoModel")
        if (focusMode == 0){
            return
        }
        when(focusMode){
            1 -> {
                selectionStart = if (pos < start) {
                    start
                } else if (pos > selectionEnd) {
                    selectionEnd
                } else {
                    pos.toLong()
                }
                selectionCurrent = selectionStart
                check()
                exoPlayer.seekTo(selectionStart)
            }
            2 -> {
                selectionEnd = if (pos < selectionStart){
                    selectionStart
                }else if (pos > end){
                    end
                }else{
                    pos.toLong()
                }
                selectionCurrent = selectionEnd
                check()
                exoPlayer.seekTo(selectionEnd)
            }
            3 -> {
                selectionCurrent = pos.toLong()
                check()
                exoPlayer.seekTo(selectionCurrent)
            }
        }
    }

    // TextureView Listener
    fun checkPosition() {
        val cur = exoPlayer.currentPosition
        selectionCurrent = if (cur > selectionEnd || cur < selectionStart) {
            exoPlayer.seekTo(selectionStart)
            selectionStart
        } else {
            cur
        }

    }

    fun getShowTime(): String {
        return "${getShowTime(selectionCurrent)} [${getShowTime(selectionStart)}-${getShowTime(selectionEnd)}]"

    }

    fun getShowTime(showTimePosition: Long): String {
        val showTimeSec = showTimePosition/1000
        return if (showTimeSec > 3600){
            "%02d:%02d:%02d".format(Locale.getDefault(), showTimeSec/3600, showTimeSec%3600/60, showTimeSec%60)
        }else{
            "%02d:%02d".format(Locale.getDefault(), showTimeSec/60, showTimeSec%60)
        }

    }

}