package com.alien.gpuimage.sources

import android.graphics.Bitmap
import com.alien.gpuimage.Framebuffer
import com.alien.gpuimage.GLContext
import com.alien.gpuimage.TextureAttributes
import com.alien.gpuimage.outputs.Output
import com.alien.gpuimage.utils.Logger

/**
 * 输出
 */
abstract class Input {

    companion object {
        private const val TAG = "Output"
    }

    protected val targets: MutableList<Output> = mutableListOf()
    protected val targetTextureIndices: MutableList<Int> = mutableListOf()
    var outputFramebuffer: Framebuffer? = null
    protected var outputTextureOptions: TextureAttributes = TextureAttributes()

    abstract fun release()

    private fun setInputFramebufferForTarget(output: Output?, inputTextureIndex: Int) {
        if (outputFramebuffer != null) {
            output?.setInputFramebuffer(outputFramebuffer, inputTextureIndex)
        }
    }

    open fun addTarget(output: Output?) {
        val index = output?.nextAvailableTextureIndex() ?: 0
        addTarget(output, index)
    }

    open fun addTarget(output: Output?, textureLocation: Int) {
        if (targets.contains(output)) {
            Logger.e(TAG, "add repeatedly targets.")
            return
        }

        output?.let {
            setInputFramebufferForTarget(it, textureLocation)
            targets.add(output)
            targetTextureIndices.add(textureLocation)
        }
    }

    fun removeTarget(output: Output?) {
        val indexOf = targets.indexOf(output)
        if (indexOf >= 0) {
            targets.removeAt(indexOf)
            targetTextureIndices.removeAt(indexOf)
        }
    }

    open fun endInput() {
        targets.forEach {
            it.endInput()
        }
    }

    fun removeTargets() {
        targets.clear()
        targetTextureIndices.clear()
    }

    /**
     * 同步运行在 GL 线程
     */
    fun runSynchronously(runnable: Runnable) {
        if (GLContext.sharedProcessingContext()?.isCurrentThread() == true) {
            runnable.run()
        } else {
            GLContext.sharedProcessingContext()?.runSynchronous(runnable)
        }
    }

    /**
     * 异步运行在 GL 线程
     */
    fun runAsynchronously(runnable: Runnable) {
        GLContext.sharedProcessingContext()?.runAsynchronously(runnable)
    }

    open fun useNextFrameForImageCapture() = Unit

    fun imageFromCurrentFramebufferWithOrientation(): Bitmap? {
        return newImageFromCurrentlyProcessedOutput()
    }

    open fun newImageFromCurrentlyProcessedOutput(): Bitmap? = null
}