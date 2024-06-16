package com.alien.gpuimage.sources

import android.graphics.SurfaceTexture
import android.view.Surface
import com.alien.gpuimage.GLContext
import com.alien.gpuimage.outputs.Input
import com.alien.gpuimage.sources.widget.GLOesTexture

/**
 * 用来 Exoplayer 上效果
 */
class ExoplayerPipeline : Output() {

    private val glOesTexture: GLOesTexture = GLOesTexture()
    private var surfaceTexture: SurfaceTexture? = null
    private var outputSurface: Surface? = null

    init {
        val create = !GLContext.contextIsExist()
        if (create) {
            GLContext(true)
            runSynchronously(Runnable { GLContext.useProcessingContext() })
        }

        // 创建 Program
        glOesTexture.createProgram()

        // 创建 surface
        surfaceTexture = SurfaceTexture(glOesTexture.getOesTexture())
        outputSurface = Surface(surfaceTexture)
        surfaceTexture?.setOnFrameAvailableListener {
            glOesTexture.onFrameAvailable(surfaceTexture, it.timestamp)
        }
    }

    fun setFormat(videoWidth: Int, videoHeight: Int, videoRotation: Int) {
        // 设置宽高方向
        glOesTexture.setOesRotation(videoRotation)
        if (videoRotation == 270 || videoRotation == 90) {
            glOesTexture.setOesSize(videoHeight, videoWidth)
        } else {
            glOesTexture.setOesSize(videoWidth, videoHeight)
        }
    }

    fun getSurface(): Surface? {
        return outputSurface
    }

    override fun addTarget(input: Input?) {
        glOesTexture.addTarget(input)
    }

    override fun addTarget(input: Input?, textureLocation: Int) {
        glOesTexture.addTarget(input, textureLocation)
    }

    override fun release() {
        glOesTexture.release()
        outputSurface?.release()
        surfaceTexture?.release()
    }
}