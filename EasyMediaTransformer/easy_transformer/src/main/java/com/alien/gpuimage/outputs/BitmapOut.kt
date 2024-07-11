package com.alien.gpuimage.outputs

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.alien.gpuimage.DataBuffer
import com.alien.gpuimage.Framebuffer
import com.alien.gpuimage.RotationMode
import com.alien.gpuimage.Size
import com.alien.gpuimage.utils.Logger
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ImageView 显示
 */
open class BitmapOut : Output {

    companion object {
        private const val TAG = "BitmapView"
    }

    private var inputFramebuffer: Framebuffer? = null
    private var fboId: Int = 0

    var callback: BitmapOutCallback? = null
    private var inBuffer: ByteBuffer? = null
    var bitmap: Bitmap? = null
        private set

    interface BitmapOutCallback {
        fun onBitmapAvailable(bitmap: Bitmap?, time: Long?)
    }

    override fun setInputSize(inputSize: Size?, textureIndex: Int) = Unit

    override fun setInputFramebuffer(framebuffer: Framebuffer?, textureIndex: Int) {
        inputFramebuffer = framebuffer
        inputFramebuffer?.lock()
    }

    override fun setInputRotation(inputRotation: RotationMode, textureIndex: Int) = Unit

    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        val start = System.nanoTime()
        Log.i(TAG, "newFrameReadyAtTime ${start}")
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
            readFboToBitmap(fboId, it.width, it.height)
            inputFramebuffer?.unlock()
        }

        Log.i(TAG, "newFrameReadyAtTime end ${System.nanoTime() - start}")
        dispatchCallback(time, bitmap)
    }

    public open fun dispatchCallback(time: Long, bitmap: Bitmap?) {
        callback?.onBitmapAvailable(bitmap, time)
    }

    open fun release() {
        if (inputFramebuffer?.onlyTexture == true) {
            if (fboId > 0) {
                runAsynchronouslyGpu(Runnable {
                    GLES20.glDeleteFramebuffers(1, intArrayOf(fboId), 0)
                    fboId = 0
                })
            }
        }
    }

    private fun readFboToBitmap(fbo: Int, width: Int, height: Int): Bitmap? {
        Logger.d(TAG, "readFboToBitmap fbo:$fbo width:$width height:$height")
        if (inBuffer == null || inBuffer?.capacity() != width * height * 4) {
            Logger.d(TAG, "create ByteBuffer")
            inBuffer?.clear()
            inBuffer = ByteBuffer.allocateDirect(width * height * 4)
            inBuffer?.order(ByteOrder.LITTLE_ENDIAN)
        }
        inBuffer?.rewind()
        inBuffer?.position(0)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo)
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, inBuffer)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        return rgbaBufferToBitmap(inBuffer, width, height)
    }

    private fun rgbaBufferToBitmap(buffer: Buffer?, width: Int, height: Int): Bitmap? {
        buffer ?: return null
        if (bitmap == null || bitmap?.width != width || bitmap?.height != height) {
            Logger.d(TAG, "create Bitmap")
            bitmap?.recycle()
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }

        bitmap?.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    /**
     * bitmap 写到纹理
     */
    fun writeBitmapToTexture(bitmap: Bitmap?, framebuffer: Framebuffer?) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, framebuffer?.textureId ?: 0)
        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }
}