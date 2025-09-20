package org.easybangumi.next.libplayer.exoplayer

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.ui.unit.IntSize
import androidx.media3.exoplayer.ExoPlayer
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.VideoSize

/**
 * Created by heyanlin on 2024/6/11.
 */
class EasyTextureView : TextureView {

    private val logger = logger()
    private val measureHelper: MeasureHelper = MeasureHelper()

    private var mainSurfaceTextureListener: SurfaceTextureListener? = null
    private var extSurfaceTextureListener: SurfaceTextureListener? = null
    private val surfaceTextureListenerWrapper: SurfaceTextureListener =
        object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                mainSurfaceTextureListener?.onSurfaceTextureAvailable(surface, width, height)
                extSurfaceTextureListener?.onSurfaceTextureAvailable(surface, width, height)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                mainSurfaceTextureListener?.onSurfaceTextureSizeChanged(surface, width, height)
                extSurfaceTextureListener?.onSurfaceTextureSizeChanged(surface, width, height)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                val res = mainSurfaceTextureListener?.onSurfaceTextureDestroyed(surface)
                extSurfaceTextureListener?.onSurfaceTextureDestroyed(surface)
                return res ?: false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // "onSurfaceTextureUpdated ${mainSurfaceTextureListener} ${extSurfaceTextureListener}".loge("EasyTextureView")
                mainSurfaceTextureListener?.onSurfaceTextureUpdated(surface)
                extSurfaceTextureListener?.onSurfaceTextureUpdated(surface)
            }
        }

    fun setVideoSize(videoSize: VideoSize) {
        logger.info("setVideoSize: $videoSize")
        measureHelper.setVideoSize(videoSize)
        requestLayout()
    }

    fun setScaleType(scaleType:  C.RendererScaleType) {
        logger.info("setScaleType: $scaleType")
        measureHelper.setScaleType(scaleType)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureHelper.doOnMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measureHelper.frameSize.width, measureHelper.frameSize.height)
        logger.info("onMeasure: widthMeasureSpec=$widthMeasureSpec, heightMeasureSpec=$heightMeasureSpec, frameSize=${measureHelper.frameSize}, frameOffset=${measureHelper.frameOffset}")
    }

    override fun setSurfaceTextureListener(listener: SurfaceTextureListener?) {
        // "setSurfaceTextureListener".loge("EasyTextureView")
        mainSurfaceTextureListener = listener
        super.setSurfaceTextureListener(surfaceTextureListenerWrapper)
    }

    override fun getSurfaceTextureListener(): SurfaceTextureListener? {
        return mainSurfaceTextureListener
    }

    fun setExtSurfaceTextureListener(listener: SurfaceTextureListener) {
        extSurfaceTextureListener = listener
    }


    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        super.setSurfaceTextureListener(surfaceTextureListenerWrapper)
    }


//    fun attachPlayer(player: ExoPlayerBridge) {
//        player.attachVideoView(this)
//    }
//
//    fun detachPlayer(player: ExoPlayerBridge) {
//        player.detachVideoView(this)
//
//    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
}