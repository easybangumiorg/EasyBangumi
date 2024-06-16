package com.alien.gpuimage.sources.widget

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.alien.gpuimage.*
import com.alien.gpuimage.sources.Output
import com.alien.gpuimage.utils.Logger
import java.nio.FloatBuffer

/**
 * 把 Oes 纹理转 普通纹理，用在相机输出，MediaCodec输出，exoplayer输出
 */
class GLOesTexture : Output() {

    companion object {
        private const val TAG = "GLOesTexture"

        private const val DEFAULT_VERTEX_SHADER =
            """
            attribute vec4 position;
            attribute vec2 inputTextureCoordinate;
            varying vec2 textureCoordinate;
            
            void main() {
                gl_Position = position;
                textureCoordinate = inputTextureCoordinate;
            }
            """

        private const val DEFAULT_FRAGMENT_SHADER =
            """
            #extension GL_OES_EGL_image_external : require
            varying mediump vec2 textureCoordinate;
            uniform samplerExternalOES inputImageTexture;
            
            void main() {
                gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
            }
            """
    }

    private var oesTexture: Int = -1
    private var oesProgram: GLProgram? = null
    private var positionAttribute: Int = 0
    private var inputTextureCoordinateAttribute: Int = 0
    private var inputImageTextureUniform: Int = 0

    private var videoRotation: RotationMode = RotationMode.NoRotation
    private var outputRotation: RotationMode = RotationMode.NoRotation
    private var oesSize: Size = Size()

    fun createProgram() {
        runSynchronously(Runnable {
            GLContext.useProcessingContext()

            // OES 纹理
            oesTexture = DataBuffer.intArray { GLES20.glGenTextures(1, it, 0) }
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTexture)
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )

            // OES Program
            oesProgram = GLContext.program(
                DEFAULT_VERTEX_SHADER,
                DEFAULT_FRAGMENT_SHADER
            )
            oesProgram?.addAttribute("position")
            oesProgram?.addAttribute("inputTextureCoordinate")

            if (oesProgram?.link() == false) {
                Logger.e(TAG, "Program link log: ${oesProgram?.programLog}")
                Logger.e(TAG, "Fragment shader compile log: ${oesProgram?.fragmentShaderLog}")
                Logger.e(TAG, "Vertex shader compile log: ${oesProgram?.vertexShaderLog}")
                oesProgram = null
                check(false) { "Filter shader link failed" }
            }

            positionAttribute = oesProgram?.attributeIndex("position") ?: 0
            inputTextureCoordinateAttribute =
                oesProgram?.attributeIndex("inputTextureCoordinate") ?: 0
            inputImageTextureUniform = oesProgram?.uniformIndex("inputImageTexture") ?: 0
        })
    }

    fun setOesRotation(rotation: Int) {
        when (rotation) {
            0 -> {
                videoRotation = RotationMode.NoRotation
            }
            270 -> {
                videoRotation = RotationMode.RotateLeft
            }
            90 -> {
                videoRotation = RotationMode.RotateRight
            }
            180 -> {
                videoRotation = RotationMode.Rotate180
            }
        }
    }

    fun setOutputRotation(rotationMode: RotationMode) {
        this.outputRotation = rotationMode
    }

    fun setOesSize(width: Int, height: Int) {
        oesSize.width = width
        oesSize.height = height
    }

    fun getOesTexture(): Int {
        return oesTexture
    }

    fun onFrameAvailable(surfaceTexture: SurfaceTexture?, presentationTimeUs: Long) {
        runSynchronously(Runnable {
            surfaceTexture?.updateTexImage()
            renderToTexture(
                DataBuffer.IMAGE_VERTICES,
                DataBuffer.textureCoordinatesForRotation(videoRotation, false)
            )
            informTargetsAboutNewFrameAtTime(presentationTimeUs)
        })
    }

    private fun renderToTexture(vertices: FloatBuffer, textureCoordinates: FloatBuffer) {
        GLContext.useProcessingContext()
        GLContext.setActiveShaderProgram(oesProgram)

        outputFramebuffer =
            GLContext.sharedFramebufferCache()
                ?.fetchFramebuffer(oesSize, false, outputTextureOptions)
        outputFramebuffer?.activate()

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTexture)
        GLES20.glUniform1i(inputImageTextureUniform, 0)

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
    }

    private fun informTargetsAboutNewFrameAtTime(time: Long) {
        targets.forEachIndexed { index, input ->
            val textureIndices = targetTextureIndices[index]
            input.setInputRotation(outputRotation, textureIndices)
            input.setInputSize(oesSize, textureIndices)
            input.setInputFramebuffer(outputFramebuffer, textureIndices)
        }

        outputFramebuffer?.unlock()
        outputFramebuffer = null

        targets.forEachIndexed { index, input ->
            val textureIndices = targetTextureIndices[index]
            input.newFrameReadyAtTime(time, textureIndices)
        }
    }

    override fun release() {
        runSynchronously(Runnable {
            if (oesTexture != -1) {
                GLES20.glDeleteTextures(1, intArrayOf(oesTexture), 0)
                oesTexture = -1
            }

            outputFramebuffer?.unlock()
            outputFramebuffer = null
            GLContext.deleteProgram(oesProgram)
        })
    }
}