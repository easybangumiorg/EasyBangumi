package com.alien.gpuimage.filter

import android.graphics.Bitmap
import android.opengl.GLES20
import android.text.TextUtils
import com.alien.gpuimage.*
import com.alien.gpuimage.outputs.Input
import com.alien.gpuimage.sources.Output
import com.alien.gpuimage.utils.Logger
import java.nio.FloatBuffer

/**
 * 滤镜基础类
 */
open class Filter(
    vertexShader: String? = DEFAULT_VERTEX_SHADER,
    fragmentShader: String? = DEFAULT_FRAGMENT_SHADER
) : Output(), Input {

    companion object {
        private const val TAG = "Filter"

        private const val DEFAULT_VERTEX_SHADER =
            """
            attribute vec4 position;
            attribute vec4 inputTextureCoordinate;
            varying vec2 textureCoordinate;
            
            void main()
            {
                gl_Position = position;
                textureCoordinate = inputTextureCoordinate.xy;
            }
            """

        private const val DEFAULT_FRAGMENT_SHADER =
            """
            varying highp vec2 textureCoordinate;
            uniform sampler2D inputImageTexture;
            
            void main()
            {
                gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
            }
            """
    }

    protected var filterProgram: GLProgram? = null
    open var positionAttribute: Int = 0
    open var inputTextureCoordinateAttribute: Int = 0
    open var inputImageTextureUniform: Int = 0

    private var inputRotation: RotationMode = RotationMode.NoRotation
    private var inputFramebuffer: Framebuffer? = null
    private var inputSize: Size? = null

    protected val backgroundColor: BackgroundColor = BackgroundColor()
    private val uniformStateRestoration = HashMap<Int, Callback>()

    protected var usingNextFrameForImageCapture = false

    init {
        this.runSynchronouslyGpu(Runnable {
            if (!TextUtils.isEmpty(vertexShader) && !TextUtils.isEmpty(fragmentShader)) {
                filterProgram = GLContext.program(vertexShader!!, fragmentShader!!)
                initializeAttributes()

                if (filterProgram?.link() == false) {
                    Logger.e(TAG, "Program link log: ${filterProgram?.programLog}")
                    Logger.e(
                        TAG,
                        "Fragment shader compile log: ${filterProgram?.fragmentShaderLog}"
                    )
                    Logger.e(TAG, "Vertex shader compile log: ${filterProgram?.vertexShaderLog}")
                    filterProgram = null
                    check(false) { "Filter shader link failed" }
                }

                positionAttribute = filterProgram?.attributeIndex("position") ?: 0
                inputTextureCoordinateAttribute =
                    filterProgram?.attributeIndex("inputTextureCoordinate") ?: 0
                inputImageTextureUniform = filterProgram?.uniformIndex("inputImageTexture") ?: 0
            }
        })
    }

    override fun setInputSize(inputSize: Size?, textureIndex: Int) {
        this.inputSize = inputSize
    }

    override fun setInputFramebuffer(framebuffer: Framebuffer?, textureIndex: Int) {
        inputFramebuffer = framebuffer
        inputFramebuffer?.lock()
    }

    override fun setInputRotation(rotation: RotationMode, textureIndex: Int) {
        inputRotation = rotation
    }

    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        renderToTexture(
            DataBuffer.IMAGE_VERTICES,
            DataBuffer.textureCoordinatesForRotation(inputRotation, false)
        )
        informTargetsAboutNewFrameAtTime(time)
    }

    override fun release() {
        runSynchronouslyGpu(Runnable {
            outputFramebuffer?.unlock()
            outputFramebuffer = null
            GLContext.deleteProgram(filterProgram)
        })
    }

    override fun useNextFrameForImageCapture() {
        usingNextFrameForImageCapture = true
    }

    override fun newImageFromCurrentlyProcessedOutput(): Bitmap? {
        usingNextFrameForImageCapture = false
        val result = outputFramebuffer?.newImageFromFramebufferContents()
        outputFramebuffer?.unlock()
        outputFramebuffer = null
        return result
    }

    open fun renderToTexture(vertices: FloatBuffer, textureCoordinates: FloatBuffer) {
        GLContext.setActiveShaderProgram(filterProgram)

        outputFramebuffer =
            GLContext.sharedFramebufferCache()
                ?.fetchFramebuffer(this.inputSize, false, outputTextureOptions)
        Logger.d(
            TAG,
            "filter in:${inputFramebuffer.toString()} out:${outputFramebuffer.toString()}"
        )
        if (usingNextFrameForImageCapture) {
            outputFramebuffer?.lock()
        }

        outputFramebuffer?.activate()
        setUniformsForProgram()

        GLES20.glClearColor(
            backgroundColor.r, backgroundColor.g,
            backgroundColor.b, backgroundColor.a
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputFramebuffer?.textureId ?: 0)
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
        inputFramebuffer?.unlock()
    }

    open fun initializeAttributes() {
        filterProgram?.addAttribute("position")
        filterProgram?.addAttribute("inputTextureCoordinate")
    }

    open fun informTargetsAboutNewFrameAtTime(time: Long) {
        targets.forEachIndexed { index, input ->
            val textureIndices = targetTextureIndices[index]
            input.setInputRotation(inputRotation, textureIndices)
            input.setInputSize(inputSize, textureIndices)
            input.setInputFramebuffer(outputFramebuffer, textureIndices)
        }

        outputFramebuffer?.unlock()
        if (!usingNextFrameForImageCapture) {
            outputFramebuffer = null
        }

        targets.forEachIndexed { index, input ->
            val textureIndices = targetTextureIndices[index]
            input.newFrameReadyAtTime(time, textureIndices)
        }
    }

    open fun rotatedSize(sizeToRotate: Size, textureIndex: Int): Size {
        val rotatedSize = Size(sizeToRotate.width, sizeToRotate.height)
        if (DataBuffer.rotationSwapsWidthAndHeight(inputRotation)) {
            rotatedSize.width = sizeToRotate.height
            rotatedSize.height = sizeToRotate.width
        }
        return rotatedSize
    }

    private fun setAndExecuteUniformStateCallbackAtIndex(
        uniform: Int,
        callback: Callback
    ) {
        uniformStateRestoration[uniform] = callback
        callback.function()
    }

    fun setUniformsForProgram() {
        uniformStateRestoration.values.forEach {
            it.function()
        }
    }

    fun getInputSize(): Size? {
        return this.inputSize
    }

    fun getInputRotation(): RotationMode {
        return this.inputRotation
    }

    fun getInputFramebuffer(): Framebuffer? {
        return this.inputFramebuffer
    }

    fun setFloat(floatValue: Float, uniform: Int, shaderProgram: GLProgram?) {
        runAsynchronouslyGpu(Runnable {
            GLContext.setActiveShaderProgram(shaderProgram)
            setAndExecuteUniformStateCallbackAtIndex(uniform, object : Callback {
                override fun function() {
                    GLES20.glUniform1f(uniform, floatValue)
                }
            })
        })
    }

    fun setUniformMatrix4f(matrix: FloatArray, uniform: Int, shaderProgram: GLProgram?) {
        runAsynchronouslyGpu(Runnable {
            GLContext.setActiveShaderProgram(shaderProgram)
            setAndExecuteUniformStateCallbackAtIndex(uniform, object : Callback {
                override fun function() {
                    GLES20.glUniformMatrix4fv(uniform, 1, false, matrix, 0)
                }
            })
        })
    }

    fun copyFrameBuffer(inputFramebuffer: Framebuffer?, outputSize: Size?): Framebuffer? {
        GLContext.setActiveShaderProgram(filterProgram)

        val copyFramebuffer =
            GLContext.sharedFramebufferCache()
                ?.fetchFramebuffer(outputSize, false, outputTextureOptions)
        Logger.d(TAG, "copyFrameBuffer copyFramebuffer:${copyFramebuffer.toString()}}")

        copyFramebuffer?.activate()
        setUniformsForProgram()

        GLES20.glClearColor(
            backgroundColor.r, backgroundColor.g,
            backgroundColor.b, backgroundColor.a
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputFramebuffer?.textureId ?: 0)
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

        return copyFramebuffer
    }
}