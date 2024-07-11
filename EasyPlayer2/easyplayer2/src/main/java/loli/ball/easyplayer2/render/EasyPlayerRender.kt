package loli.ball.easyplayer2.render

import android.content.Context
import android.view.View
import androidx.media3.exoplayer.ExoPlayer

/**
 * Created by heyanle on 2024/6/12.
 * https://github.com/heyanLE
 */
interface EasyPlayerRender {

    fun getViewOrNull(): View?

    fun getOrCreateView(ctx: Context): View

    fun onAttachToPlayer(exoPlayer: ExoPlayer)

    fun onDetachToPlayer(exoPlayer: ExoPlayer)

    fun setScaleType(scaleType: Int)

    fun setVideoSize(width: Int, height: Int)

    fun setVideoRotation(degree: Int)


}