package com.alien.gpuimage.filter

import android.opengl.GLES20
import com.alien.gpuimage.DataBuffer
import com.alien.gpuimage.Framebuffer
import com.alien.gpuimage.GLContext
import com.alien.gpuimage.utils.Logger
import java.nio.FloatBuffer
import kotlin.math.abs

/**
 * 丢帧滤镜 控制帧率
 * input -> temp -> output -> target
 * 每次收到新的 input 都将 output 更新为 temp，然后将 temp 更新为 input
 *
 * 当以下条件满足其中一条时才将 output 发送给 target
 * 1. 是第一帧
 * 2. 不是第二帧且 output 与上一次消费的时间差与 delayTime 的差值小于当前帧与上一次消费的时间差与 delayTime 的差值
 * 其中 delayTime = 1000000/fps
 *
 * Created by heyanle on 2024/6/29.
 * https://github.com/heyanLE
 */
class DropFrameFilter(
    private val fps: Float
): Filter() {

    companion object {
        const val TAG = "DropFrameFilter"
    }


    private val delayTime = (1000000/fps).toLong()
    private var tempFramebuffer: Framebuffer? = null
    private var tempFrameTime: Long = Long.MAX_VALUE

    private var outputFrameTime: Long = Long.MAX_VALUE

    private var framesReceived = 0
    private var lastConsumeTime = 0L


    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        framesReceived ++
        feedTempFrameFromInput(time)
        if (framesReceived == 1){
            feedOutputFrameFromTemp()
            feedTargetFromOutput()
        }

        if (shouldFeedTarget(time)) {
            feedTargetFromOutput()
        }
        feedOutputFrameFromTemp()

    }

    private fun shouldFeedTarget(time: Long) : Boolean {
        if (framesReceived == 2){
            // The previous texture has already been queued when it's the first texture.
            return false
        }

        val outputFrameTimeDeltaUs = outputFrameTime - lastConsumeTime
        val currentFrameTimeDeltaUs = time - lastConsumeTime

        return abs(outputFrameTimeDeltaUs - delayTime) <= abs(currentFrameTimeDeltaUs - delayTime)
    }




    // input -> temp
    private fun feedTempFrameFromInput(time: Long) {
        renderToTexture(
            DataBuffer.IMAGE_VERTICES,
            DataBuffer.textureCoordinatesForRotation(innerInputRotation, false)
        )
        outputFrameTime = time
    }

    // output -> temp
    private fun feedOutputFrameFromTemp() {
        if (tempFramebuffer == null){
            return
        }
        GLContext.setActiveShaderProgram(filterProgram)
        initOutputFramebufferIfNeeded()
        val copyFramebuffer = outputFramebuffer

        copyFramebuffer?.activate()
        setUniformsForProgram()

        GLES20.glClearColor(
            backgroundColor.r, backgroundColor.g,
            backgroundColor.b, backgroundColor.a
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tempFramebuffer?.textureId ?: 0)
        GLES20.glUniform1i(inputImageTextureUniform, 2)

        // GL坐标
        GLES20.glEnableVertexAttribArray(positionAttribute)
        GLES20.glVertexAttribPointer(
            positionAttribute,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            DataBuffer.IMAGE_VERTICES
        )

        // 纹理坐标
        GLES20.glEnableVertexAttribArray(inputTextureCoordinateAttribute)
        GLES20.glVertexAttribPointer(
            inputTextureCoordinateAttribute,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            DataBuffer.textureCoordinatesForRotation(getInputRotation(), false)
        )

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // 关闭 属性
        GLES20.glDisableVertexAttribArray(positionAttribute)
        GLES20.glDisableVertexAttribArray(inputTextureCoordinateAttribute)

        tempFrameTime = outputFrameTime
    }


    // output -> target
    private fun feedTargetFromOutput() {
        if (outputFramebuffer == null){
            return
        }
        lastConsumeTime = outputFrameTime
        targets.forEachIndexed { index, input ->
            val textureIndices = targetTextureIndices[index]
            input.setInputRotation(innerInputRotation, textureIndices)
            input.setInputSize(innerInputSize, textureIndices)
            input.setInputFramebuffer(outputFramebuffer, textureIndices)
        }
        targets.forEachIndexed { index, input ->
            val textureIndices = targetTextureIndices[index]
            input.newFrameReadyAtTime(outputFrameTime, textureIndices)
        }
    }


    private fun initOutputFramebufferIfNeeded() {
        if (outputFramebuffer == null) {
            outputFramebuffer =
                GLContext.sharedFramebufferCache()
                    ?.fetchFramebuffer(this.innerInputSize, false, outputTextureOptions)
            Logger.d(
                TAG,
                "filter in:${innerInputFramebuffer.toString()} out:${outputFramebuffer.toString()}"
            )
            // 直接 lock 住防止回收
            outputFramebuffer?.lock()
        }
    }
    private fun initTempFramebufferIfNeeded() {
        if (tempFramebuffer == null){
            tempFramebuffer =
                GLContext.sharedFramebufferCache()
                    ?.fetchFramebuffer(this.innerInputSize, false, outputTextureOptions)
            Logger.d(
                TAG,
                "filter in:${innerInputFramebuffer.toString()} out:${outputFramebuffer.toString()}"
            )
            // 直接 lock 住防止回收
            tempFramebuffer?.lock()
        }
    }

    override fun renderToTexture(vertices: FloatBuffer, textureCoordinates: FloatBuffer) {
        GLContext.setActiveShaderProgram(filterProgram)

        initTempFramebufferIfNeeded()

        tempFramebuffer?.activate()
        setUniformsForProgram()

        GLES20.glClearColor(
            backgroundColor.r, backgroundColor.g,
            backgroundColor.b, backgroundColor.a
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, innerInputFramebuffer?.textureId ?: 0)
        GLES20.glUniform1i(inputImageTextureUniform, 2)

        // GL坐标
        GLES20.glEnableVertexAttribArray(positionAttribute)
        GLES20.glVertexAttribPointer(positionAttribute, 2, GLES20.GL_FLOAT, false, 0, vertices)

        // 纹理坐标
        GLES20.glEnableVertexAttribArray(inputTextureCoordinateAttribute)
        GLES20.glVertexAttribPointer(
            inputTextureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 0, textureCoordinates
        )

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // 关闭 属性
        GLES20.glDisableVertexAttribArray(positionAttribute)
        GLES20.glDisableVertexAttribArray(inputTextureCoordinateAttribute)

        // 输入释放fbo
        innerInputFramebuffer?.unlock()
    }

    override fun release() {
        super.release()
        framesReceived = 0
        outputFramebuffer?.unlock()
        tempFramebuffer?.unlock()
    }

}