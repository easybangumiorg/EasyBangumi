package com.alien.gpuimage.sources

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.alien.gpuimage.Callback
import com.alien.gpuimage.CallbackParam
import com.alien.gpuimage.GLContext
import com.alien.gpuimage.Size
import com.alien.gpuimage.utils.Logger

/**
 * Bitmap 输入
 */
class Picture(bitmap: Bitmap? = null, recycle: Boolean) : Input() {

    companion object {
        private const val TAG = "Picture"
    }

    private var bitmap: Bitmap? = null
    private var recycle: Boolean = false

    private var pixelSizeOfImage: Size? = null
    private var processRunnable = ProcessRunnable()

    init {
        init(bitmap, recycle)
    }

    private fun init(bitmap: Bitmap?, recycle: Boolean) {
        val create = !GLContext.contextIsExist()
        if (create) {
            GLContext(true)
            runSynchronously(Runnable { GLContext.useProcessingContext() })
        }

        runSynchronously(Runnable {
            this.recycle = recycle
            if (bitmap != null && !bitmap.isRecycled) {
                this.bitmap = bitmap
                loadImageToFBO()
                if (this.recycle) {
                    this.bitmap?.recycle()
                }
            }
        })
    }

    fun setBitmap(bitmap: Bitmap?) {
        runSynchronously(Runnable {
            if (bitmap != null && !bitmap.isRecycled) {
                this.bitmap = bitmap
                release()
                loadImageToFBO()
                if (this.recycle) {
                    this.bitmap?.recycle()
                }
            }
        })
    }

    fun processPictureSingle() {
        val handle = GLContext.sharedProcessingContext()?.getCurrentHandler()
        if (handle?.hasCallbacks(processRunnable) == true) {
            handle.removeCallbacks(processRunnable)
        }
        runAsynchronously(processRunnable)
    }

    fun processPicture() {
        runAsynchronously(Runnable {
            processRunnable.run()
        })
    }

    fun processPicture(completion: Callback) {
        runAsynchronously(Runnable {
            processRunnable.run()
            completion.function()
        })
    }

    fun processPictureSynchronously() {
        runSynchronously(Runnable {
            processRunnable.run()
        })
    }

    fun processImageUpToFilter(finalFilterInputChain: Input?, completion: CallbackParam<Bitmap?>) {
        runAsynchronously(Runnable {
            finalFilterInputChain?.useNextFrameForImageCapture()
            processRunnable.run()
            val imageFromFilter = finalFilterInputChain?.imageFromCurrentFramebufferWithOrientation()
            completion.function(imageFromFilter)
        })
    }

    private fun loadImageToFBO() {
        check(bitmap?.width ?: 0 > 0 && bitmap?.height ?: 0 > 0)

        GLContext.useProcessingContext()
        pixelSizeOfImage = GLContext.withinTextureForSize(Size(bitmap!!.width, bitmap!!.height))
        outputFramebuffer =
            GLContext.sharedFramebufferCache()?.fetchFramebuffer(pixelSizeOfImage, false)
        outputFramebuffer?.disableReferenceCounting()
        Logger.d(TAG, "picture out ${outputFramebuffer.toString()}")

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, outputFramebuffer?.textureId ?: 0)
        GLUtils.texImage2D(
            GLES20.GL_TEXTURE_2D, 0,
            GLES20.GL_RGBA, bitmap, GLES20.GL_UNSIGNED_BYTE, 0
        )
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun release() {
        runSynchronously(Runnable {
            outputFramebuffer?.let {
                it.enableReferenceCounting()
                it.unlock()
            }
        })
    }

    private inner class ProcessRunnable : Runnable {
        override fun run() {
            if (pixelSizeOfImage == null) return
            val time = System.currentTimeMillis()
            targets.forEachIndexed { index, input ->
                val textureIndices = targetTextureIndices[index]
                input.setInputSize(pixelSizeOfImage, textureIndices)
                input.setInputFramebuffer(outputFramebuffer, textureIndices)
                input.newFrameReadyAtTime(System.currentTimeMillis(), textureIndices)
            }
            Logger.d(TAG, "picture time:${System.currentTimeMillis() - time}")
        }
    }
}