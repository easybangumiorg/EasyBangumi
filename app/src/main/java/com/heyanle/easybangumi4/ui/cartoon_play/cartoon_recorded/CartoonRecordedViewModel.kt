package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import loli.ball.easyplayer2.texture.EasyTextureView

/**
 * Created by heyanlin on 2024/6/11.
 */
class CartoonRecordedViewModel(
    private val exoPlayer: ExoPlayer,
) : ViewModel() {

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

    @SuppressLint("StaticFieldLeak")
    val textureView: EasyTextureView = EasyTextureView(APP)

    fun onLaunch(){
        exoPlayer.clearVideoSurface()
        exoPlayer.setVideoTextureView(textureView)
    }

    fun onDispose(){
        exoPlayer.clearVideoSurface()
    }

    fun startRecord(){}

    fun stopRecord(){}

    fun makeGif(){}

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