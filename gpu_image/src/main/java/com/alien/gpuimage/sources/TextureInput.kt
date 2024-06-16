package com.alien.gpuimage.sources

import android.opengl.GLES20
import com.alien.gpuimage.Framebuffer
import com.alien.gpuimage.Size

/**
 * 纹理输入（待使用）
 */
class TextureInput : Output() {

    private var textureSize: Size? = null

    fun initWithTexture(texture: Int, width: Int, height: Int) {
        runSynchronously(Runnable {
            textureSize = Size(width, height)
            outputFramebuffer = Framebuffer(width, height, texture)
        })
    }

    fun processTextureWithFrameTime(frameTime: Long) {
        runAsynchronously(Runnable {
            targets.forEachIndexed { index, input ->
                val textureIndices = targetTextureIndices[index]
                input.setInputSize(textureSize, textureIndices)
                input.setInputFramebuffer(outputFramebuffer, textureIndices)
                input.newFrameReadyAtTime(frameTime, textureIndices)
            }
        })
    }

    override fun release() {
        runAsynchronously(Runnable {
            if (outputFramebuffer?.framebufferId ?: 0 > 0) {
                GLES20.glDeleteFramebuffers(1, intArrayOf(outputFramebuffer!!.framebufferId), 0)
            }
            outputFramebuffer = null
        })
    }
}