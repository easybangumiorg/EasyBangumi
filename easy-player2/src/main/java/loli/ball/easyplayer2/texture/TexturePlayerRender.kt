package loli.ball.easyplayer2.texture

import android.content.Context
import android.view.TextureView
import android.view.View
import androidx.media3.exoplayer.ExoPlayer
import loli.ball.easyplayer2.render.EasyPlayerRender

/**
 * Created by heyanle on 2024/6/12.
 * https://github.com/heyanLE
 */
class TexturePlayerRender: EasyPlayerRender {
    private var surfaceTextureListener: TextureView.SurfaceTextureListener? = null
    private var textureView: EasyTextureView? = null

    override fun getViewOrNull(): View? {
        return textureView
    }

    fun getTextureViewOrNull(): EasyTextureView? {
        return textureView
    }

    override fun getOrCreateView(ctx: Context): View {
        val sur = textureView
        if (sur == null){
            val surfaceView = EasyTextureView(ctx)
            this.textureView = surfaceView
            surfaceTextureListener?.let {
                surfaceView.setExtSurfaceTextureListener(it)
            }
            return surfaceView
        }
        return sur
    }

    override fun onAttachToPlayer(exoPlayer: ExoPlayer) {
        val text = textureView ?: return
        exoPlayer.setVideoTextureView(text)
    }

    override fun onDetachToPlayer(exoPlayer: ExoPlayer) {
        val text = textureView ?: return
        exoPlayer.clearVideoTextureView(text)
    }

    override fun setScaleType(scaleType: Int) {
        textureView?.setScaleType(scaleType)
    }

    override fun setVideoSize(width: Int, height: Int) {
        textureView?.setVideoSize(width, height)
    }

    override fun setVideoRotation(degree: Int) {
        textureView?.setVideoRotation(degree)
    }

    fun setExtSurfaceTextureListener(listener: TextureView.SurfaceTextureListener){
        textureView?.setExtSurfaceTextureListener(listener)
        this.surfaceTextureListener = listener
    }
}