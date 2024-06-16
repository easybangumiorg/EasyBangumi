package com.alien.gpuimage.outputs

import com.alien.gpuimage.Framebuffer
import com.alien.gpuimage.GLContext
import com.alien.gpuimage.RotationMode
import com.alien.gpuimage.Size

/**
 * 输入接口
 */
interface Input {

    fun setInputSize(inputSize: Size?, textureIndex: Int)

    fun setInputFramebuffer(framebuffer: Framebuffer?, textureIndex: Int)

    fun setInputRotation(inputRotation: RotationMode, textureIndex: Int)

    fun newFrameReadyAtTime(time: Long, textureIndex: Int)

    fun nextAvailableTextureIndex(): Int = 0

    /**
     * 同步运行在 GL 线程
     */
    fun runSynchronouslyGpu(runnable: Runnable) {
        if (GLContext.sharedProcessingContext()?.isCurrentThread() == true) {
            runnable.run()
        } else {
            GLContext.sharedProcessingContext()?.runSynchronous(runnable)
        }
    }

    /**
     * 异步运行在 GL 线程
     */
    fun runAsynchronouslyGpu(runnable: Runnable) {
        GLContext.sharedProcessingContext()?.runAsynchronously(runnable)
    }
}