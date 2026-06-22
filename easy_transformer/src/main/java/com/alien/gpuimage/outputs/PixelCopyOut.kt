package com.alien.gpuimage.outputs

import android.graphics.Bitmap
import android.opengl.GLES20
import com.alien.gpuimage.DataBuffer
import com.alien.gpuimage.Framebuffer
import com.alien.gpuimage.RotationMode
import com.alien.gpuimage.Size
import com.alien.gpuimage.utils.Logger
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by heyanle on 2024/6/27.
 * https://github.com/heyanLE
 */
class PixelCopyOut: Output {
    
    companion object {
        private const val TAG = "PixelCopyOut"
    }

    interface Callback {
        fun onPixelFrameReady(byteBuffer: ByteBuffer, time: Long)
    }


    private var inputFramebuffer: Framebuffer? = null
    private var fboId: Int = 0

    
    private var outputBuffer: ByteBuffer? = null
    var callback: PixelCopyOut.Callback? = null
    

    override fun setInputSize(inputSize: Size?, textureIndex: Int) { }

    override fun setInputRotation(inputRotation: RotationMode, textureIndex: Int) { }

    override fun setInputFramebuffer(framebuffer: Framebuffer?, textureIndex: Int) {
        inputFramebuffer = framebuffer
        inputFramebuffer?.lock()
    }

   

    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        inputFramebuffer?.let { it ->
            if (!it.onlyTexture) {
                fboId = it.framebufferId
            } else {
                if (fboId == 0) {
                    fboId = DataBuffer.intArray { fbo -> GLES20.glGenFramebuffers(1, fbo, 0) }
                    Logger.d(TAG, "create fbo $fboId")
                }
            }

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                it.textureId,
                0
            )
            val fboStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (fboStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Logger.e(TAG, "initFBO failed, status: $fboStatus")
            }

            Logger.d(TAG, "imageView in ${inputFramebuffer.toString()}")

            if (outputBuffer == null || outputBuffer?.capacity() != it.width * it.height * 4) {
                Logger.d(TAG, "create ByteBuffer")
                outputBuffer?.clear()
                outputBuffer = ByteBuffer.allocateDirect(it.width * it.height * 4)
                outputBuffer?.order(ByteOrder.LITTLE_ENDIAN)
            }
            outputBuffer?.rewind()
            outputBuffer?.position(0)

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)
            GLES20.glReadPixels(0, 0, it.width, it.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, outputBuffer)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            outputBuffer?.let { buffer ->
                callback?.onPixelFrameReady(buffer, time)
            }
            inputFramebuffer?.unlock()
        }
    }
}