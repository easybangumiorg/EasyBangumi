package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import loli.ball.easyplayer2.surface.EasySurfaceView
import loli.ball.easyplayer2.texture.EasyTextureView

/**
 * Created by heyanlin on 2024/6/11.
 */
class CartoonRecordedViewModel(
    private val exoPlayer: ExoPlayer,
) : ViewModel(),  Player.Listener {

    data class Configuration(
        val fps: Int = 15,

        // 1->gif 2->mp4
        val type: Int = 1,
    )

    data class RecordedState(
        // 0->idle 1->recording 2->saving 3->completely
        val state: Int = 0,
        val configuration: Configuration = Configuration(),
    )

    private val _stateFlow = MutableStateFlow(RecordedState())
    val stateFlow = _stateFlow.asStateFlow()

    @SuppressLint("StaticFieldLeak")
    val layout = LayoutInflater.from(APP).inflate(R.layout.recorded_layout, null)


    @SuppressLint("StaticFieldLeak")
    val textureView: EasyTextureView = layout.findViewById(R.id.easy_texture)

    @SuppressLint("StaticFieldLeak")
    val rectSelectionView: RectSelectionView = layout.findViewById(R.id.rect_selection_view)

    fun onLaunch(){
        exoPlayer.addListener(this)
        //exoPlayer.clearVideoSurface()
        exoPlayer.clearVideoSurface()
        textureView.attachPlayer(exoPlayer)
    }

    fun onDispose(){
        exoPlayer.removeListener(this)
        textureView.detachPlayer(exoPlayer)
        //exoPlayer.clearVideoTextureView(textureView)
    }

    fun startRecord(){}

    fun stopRecord(){}

    fun makeGif(){}

    fun changeGif(){}

    fun changeMp4(){}

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        textureView.setVideoSize(videoSize.width, videoSize.height)
    }



}

class CartoonRecordedViewModelFactory(
    private val exoPlayer: ExoPlayer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartoonRecordedViewModel::class.java))
            return CartoonRecordedViewModel(exoPlayer) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}