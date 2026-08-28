package loli.ball.easyplayer2.surface

import android.content.Context
import android.view.View
import loli.ball.easyplayer2.render.EasyPlayerRender

/**
 * Created by heyanle on 2024/6/12.
 * https://github.com/heyanLE
 */
class SurfacePlayerRender: EasyPlayerRender {

    private var surfaceView: EasySurfaceView? = null

    override fun getViewOrNull(): View? {
        return surfaceView
    }

    override fun getOrCreateView(ctx: Context): View {
        val sur = surfaceView
        if (sur == null){
            val surfaceView = EasySurfaceView(ctx).apply {
                // AndroidView hosts a real child View above the Compose hit-test tree. Keeping the
                // video surface non-interactive lets the foreground Compose gesture/controller
                // layer receive taps, double taps and drags for both ExoPlayer and libmpv.
                isClickable = false
                isLongClickable = false
                isFocusable = false
                isFocusableInTouchMode = false
            }
            this.surfaceView = surfaceView
            return surfaceView
        }
        return sur
    }

    override fun setScaleType(scaleType: Int) {
        surfaceView?.setScaleType(scaleType)
    }

    override fun setVideoSize(width: Int, height: Int) {
        surfaceView?.setVideoSize(width, height)
    }

    override fun setVideoRotation(degree: Int) {
        surfaceView?.setVideoRotation(degree)
    }
}
