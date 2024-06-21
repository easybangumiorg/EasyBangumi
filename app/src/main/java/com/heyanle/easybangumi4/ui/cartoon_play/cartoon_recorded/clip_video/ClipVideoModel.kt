package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.exo.thumbnail.OutputThumbnailHelper
import com.heyanle.easybangumi4.exo.thumbnail.ThumbnailBuffer
import com.heyanle.easybangumi4.utils.dip2px
import com.heyanle.easybangumi4.utils.getCachePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.TreeMap

/**
 * Created by heyanlin on 2024/6/21.
 */
@UnstableApi
class ClipVideoModel(
    val ctx: Context,
    val exoPlayer: ExoPlayer,
    val mediaItem: MediaItem,
    val mediaSourceFactory: MediaSource.Factory,

    val scope: CoroutineScope,

    val thumbnailBuffer: ThumbnailBuffer,

    val start: Long,
    val end: Long,

    val currentPosition: Long,
) {

    companion object {
        const val horizontalPaddingDp = 18
        const val verticalPaddingDp = 6
    }

    val horizontalPaddingPx = dip2px(ctx, horizontalPaddingDp.toFloat())
    val verticalPaddingPx = dip2px(ctx, verticalPaddingDp.toFloat())

    // 剪辑配置
    data class ClipVideoState(
        // 进度
        val selectionCurrent: Long,
        val selectionStart: Long,
        val selectionEnd: Long,
    ){
        fun copyWithCheck(): ClipVideoState {
            if (selectionEnd < selectionStart){
                return copy(selectionEnd = selectionStart, selectionCurrent = selectionStart)
            }
            if (selectionCurrent < selectionStart){
                return copy(selectionCurrent = selectionStart)
            }
            if (selectionCurrent > selectionEnd){
                return copy(selectionCurrent = selectionEnd)
            }
            return this
        }

        fun check(): Boolean {
            return selectionEnd >= selectionStart && selectionCurrent >= selectionStart && selectionCurrent in selectionStart..selectionEnd
        }


    }

    // 运行时一些参数
    data class RuntimeState(
        val jpgPositionList: List<Long> = listOf(),
        val bmpTreeMap: TreeMap<Long, File> = TreeMap(),

        // 待定系数
        // 视频时间轴 [start, end]
        // View 可滑动部分 [horizontalPadding, width - horizontalPadding]
        val pos2Px : Pair<Float, Float> = 0f to 0f,
        val px2Pos : Pair<Float, Float> = 0f to 0f,

        // 0: normal, 1: select start, 2: select end, 3: select position
        val focusMode: Int = 0,
    )

    // 渲染相关参数 - 跟着页面布局周期
    data class WidgetState(
        val clipSeekBoxWidth: Int = 0,
        val clipSeekBoxHeight: Int = 0,
        val thumbnailVideoSize: VideoSize = VideoSize(0, 0),
    )

    private val _clipVideoState = MutableStateFlow(ClipVideoState(currentPosition, start, end))
    val clipVideoState = _clipVideoState.asStateFlow()

    private val _widgetState = MutableStateFlow(WidgetState())
    val widgetState = _widgetState.asStateFlow()

    private val _runtimeState = MutableStateFlow(RuntimeState())
    val runtimeState = _runtimeState.asStateFlow()

    // 加载缩略图
    val outputThumbnailHelper = OutputThumbnailHelper(
        ctx,
        mediaSourceFactory.createMediaSource(mediaItem),
        File(APP.getCachePath("thumbnail")),
        thumbnailBuffer,
        start,
        end,
        5000
    ).apply {
        this.onVideoSizeChange = { size ->
            if (size.width > 0 && size.height > 0) {
                scope.launch {
                    _widgetState.update {
                        it.copy(
                            thumbnailVideoSize = size
                        )
                    }
                }
            }
        }
    }

    init {
        scope.launch {
            widgetState.collectLatest { state ->
                // 待定系数计算
                val k = (state.clipSeekBoxWidth - horizontalPaddingPx) / (end - start).toFloat()
                val b = horizontalPaddingPx - start * k
                val pos2Px = k to b

                val k2 = (end - start) / (state.clipSeekBoxWidth - 2 * horizontalPaddingPx).toFloat()
                val b2 = start - horizontalPaddingPx * k2
                val px2Pos = k2 to b2

                // 缩略图 position 计算
                val pngWidth = state.clipSeekBoxHeight
                var currentPx = horizontalPaddingPx
                val jpgPositionList = arrayListOf<Long>()
                while(currentPx <= state.clipSeekBoxWidth - horizontalPaddingPx){
                    val pos = k2 * currentPx + b2
                    jpgPositionList.add(pos.toLong())
                    currentPx += pngWidth
                }

                _runtimeState.update {
                    it.copy(
                        pos2Px = pos2Px,
                        px2Pos = px2Pos,
                        jpgPositionList = jpgPositionList
                    )
                }


            }
        }

        scope.launch {
            outputThumbnailHelper.start()
        }
    }


    fun onSizeChange(width: Int, height: Int){
        _widgetState.update {
            it.copy(
                clipSeekBoxWidth = width,
                clipSeekBoxHeight = height
            )
        }
    }

    fun onFocusChange(focus: Int){
        _runtimeState.update {
            it.copy(
                focusMode = focus
            )
        }
        if (focus > 0)
            exoPlayer.playWhenReady = false
    }

    fun onSelectionChange(pos: Float, focusMode: Int){
        _clipVideoState.update {
            when(focusMode){
                1 -> it.copy(selectionCurrent = pos.toLong())
                2 -> it.copy(selectionStart = pos.toLong())
                3 -> it.copy(selectionEnd = pos.toLong())
                else -> it
            }.copyWithCheck().apply {
                exoPlayer.seekTo(selectionCurrent)
            }
        }
    }

    // TextureView Listener
    fun checkPosition() {
        if (exoPlayer.isPlaying && exoPlayer.playWhenReady){
            _clipVideoState.update {
                it.copy(
                    selectionCurrent = exoPlayer.currentPosition
                ).copyWithCheck()
            }
        }
    }

}