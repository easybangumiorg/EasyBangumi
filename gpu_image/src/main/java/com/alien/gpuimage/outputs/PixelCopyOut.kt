package com.alien.gpuimage.outputs

import com.alien.gpuimage.Framebuffer
import com.alien.gpuimage.RotationMode
import com.alien.gpuimage.Size
import java.nio.ByteBuffer

/**
 * Created by heyanle on 2024/6/27.
 * https://github.com/heyanLE
 */
class PixelCopyOut: Output {

    private var inputFramebuffer: Framebuffer? = null
    private var outputFrame: ByteBuffer? = null

    override fun setInputSize(inputSize: Size?, textureIndex: Int) { }

    override fun setInputRotation(inputRotation: RotationMode, textureIndex: Int) { }

    override fun setInputFramebuffer(framebuffer: Framebuffer?, textureIndex: Int) {
        inputFramebuffer = framebuffer
        inputFramebuffer?.lock()
    }



    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        TODO("Not yet implemented")
    }
}