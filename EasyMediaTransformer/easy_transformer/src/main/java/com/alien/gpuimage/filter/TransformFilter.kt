package com.alien.gpuimage.filter

import android.opengl.Matrix
import com.alien.gpuimage.DataBuffer
import com.alien.gpuimage.Size
import java.nio.FloatBuffer

/**
 * 可用来移动，缩放，矩阵操作
 */
class TransformFilter : Filter(vertexShader = VERTEX_SHADER) {

    companion object {
        private const val VERTEX_SHADER =
            """
            attribute vec4 position;
            attribute vec4 inputTextureCoordinate;
            
            uniform mat4 transformMatrix;
            uniform mat4 orthographicMatrix;
            
            varying vec2 textureCoordinate;
            
            void main()
            {
                gl_Position = transformMatrix * vec4(position.xyz, 1.0) * orthographicMatrix;
                textureCoordinate = inputTextureCoordinate.xy;
            }
            """

        val squareVertices =
            DataBuffer.createFloatBuffer(
                floatArrayOf(
                    -1.0f, -1.0f,
                    1.0f, -1.0f,
                    -1.0f, 1.0f,
                    1.0f, 1.0f
                )
            )

        val squareVerticesAnchorTL =
            DataBuffer.createFloatBuffer(
                floatArrayOf(
                    0.0f, 0.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f
                )
            )
    }

    private var transformMatrixUniform = 0
    private var transform3D: FloatArray? = null
    private var orthographicMatrixUniform = 0
    private var orthographicMatrix: FloatArray? = null

    private var ignoreAspectRatio = false
    private var anchorTopLeft = false

    private var adjustedVerticesAnchorTL: FloatArray = FloatArray(8)
    private var adjustedVerticesAnchorTLFb: FloatBuffer =
        DataBuffer.createFloatBuffer(adjustedVerticesAnchorTL)

    private var adjustedVertices: FloatArray = FloatArray(8)
    private var adjustedVerticesFb: FloatBuffer = DataBuffer.createFloatBuffer(adjustedVertices)

    init {
        orthographicMatrix = FloatArray(16)
        loadOrthoMatrix(orthographicMatrix, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f)
        transform3D = FloatArray(16)
        Matrix.setIdentityM(transform3D, 0)

        runSynchronouslyGpu(Runnable {
            transformMatrixUniform = filterProgram?.uniformIndex("transformMatrix") ?: 0
            orthographicMatrixUniform = filterProgram?.uniformIndex("orthographicMatrix") ?: 0
            setUniformMatrix4f(transform3D!!, transformMatrixUniform, filterProgram)
            setUniformMatrix4f(orthographicMatrix!!, orthographicMatrixUniform, filterProgram)
        })
    }

    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        if (ignoreAspectRatio) {
            if (anchorTopLeft) {
                renderToTexture(
                    squareVerticesAnchorTL,
                    DataBuffer.textureCoordinatesForRotation(getInputRotation(), false)
                )
            } else {
                renderToTexture(
                    squareVertices,
                    DataBuffer.textureCoordinatesForRotation(getInputRotation(), false)
                )
            }
        } else {
            val inputSize = getInputSize()!!
            val normalizedHeight = inputSize.height.toFloat() / inputSize.width.toFloat()
            setupFilterForSize(inputSize)

            if (anchorTopLeft) {
                adjustedVerticesAnchorTL[0] = 0.0f
                adjustedVerticesAnchorTL[1] = 0.0f
                adjustedVerticesAnchorTL[2] = 1.0f
                adjustedVerticesAnchorTL[3] = 0.0f
                adjustedVerticesAnchorTL[4] = 0.0f
                adjustedVerticesAnchorTL[5] = normalizedHeight
                adjustedVerticesAnchorTL[6] = 1.0f
                adjustedVerticesAnchorTL[7] = normalizedHeight
                adjustedVerticesAnchorTLFb.rewind()
                adjustedVerticesAnchorTLFb.put(adjustedVerticesAnchorTL)
                adjustedVerticesAnchorTLFb.position(0)

                renderToTexture(
                    adjustedVerticesAnchorTLFb,
                    DataBuffer.textureCoordinatesForRotation(getInputRotation(), false)
                )
            } else {
                adjustedVertices[0] = -1.0f
                adjustedVertices[1] = -normalizedHeight
                adjustedVertices[2] = 1.0f
                adjustedVertices[3] = -normalizedHeight
                adjustedVertices[4] = -1.0f
                adjustedVertices[5] = normalizedHeight
                adjustedVertices[6] = 1.0f
                adjustedVertices[7] = normalizedHeight
                adjustedVerticesFb.rewind()
                adjustedVerticesFb.put(adjustedVertices)
                adjustedVerticesFb.position(0)

                renderToTexture(
                    adjustedVerticesFb,
                    DataBuffer.textureCoordinatesForRotation(getInputRotation(), false)
                )
            }
        }

        informTargetsAboutNewFrameAtTime(time)
    }

    private fun setupFilterForSize(inputSize: Size?) {
        if (!ignoreAspectRatio && inputSize != null) {
            loadOrthoMatrix(
                orthographicMatrix,
                -1.0f,
                1.0f,
                -1.0f * inputSize.height.toFloat() / inputSize.width.toFloat(),
                1.0f * inputSize.height.toFloat() / inputSize.width.toFloat(),
                -1.0f,
                1.0f
            )
            setUniformMatrix4f(orthographicMatrix!!, orthographicMatrixUniform, filterProgram)
        }
    }

    fun setTransform3D(transform3D: FloatArray?) {
        this.transform3D = transform3D
        setUniformMatrix4f(this.transform3D!!, transformMatrixUniform, filterProgram)
    }

    fun getTransform3D(): FloatArray? {
        return transform3D
    }

    fun setIgnoreAspectRatio(ignoreAspectRatio: Boolean) {
        this.ignoreAspectRatio = ignoreAspectRatio
        if (this.ignoreAspectRatio) {
            loadOrthoMatrix(orthographicMatrix, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f)
            setUniformMatrix4f(orthographicMatrix!!, orthographicMatrixUniform, filterProgram)
        } else {
            setupFilterForSize(getInputSize())
        }
    }

    fun ignoreAspectRatio(): Boolean {
        return ignoreAspectRatio
    }

    fun setAnchorTopLeft(anchorTopLeft: Boolean) {
        this.anchorTopLeft = anchorTopLeft
        setIgnoreAspectRatio(ignoreAspectRatio)
    }

    fun anchorTopLeft(): Boolean {
        return anchorTopLeft
    }

    private fun loadOrthoMatrix(
        matrix: FloatArray?,
        left: Float, right: Float, bottom: Float, top: Float,
        near: Float, far: Float
    ) {
        val r_l = right - left
        val t_b = top - bottom
        val f_n = far - near
        var tx = -(right + left) / (right - left)
        var ty = -(top + bottom) / (top - bottom)
        val tz = -(far + near) / (far - near)

        var scale = 2.0f
        if (anchorTopLeft) {
            scale = 4.0f
            tx = -1.0f
            ty = -1.0f
        }

        matrix?.set(0, scale / r_l)
        matrix?.set(1, 0.0f)
        matrix?.set(2, 0.0f)
        matrix?.set(3, tx)

        matrix?.set(4, 0.0f)
        matrix?.set(5, scale / t_b)
        matrix?.set(6, 0.0f)
        matrix?.set(7, ty)

        matrix?.set(8, 0.0f)
        matrix?.set(9, 0.0f)
        matrix?.set(10, scale / f_n)
        matrix?.set(11, tz)

        matrix?.set(12, 0.0f)
        matrix?.set(13, 0.0f)
        matrix?.set(14, 0.0f)
        matrix?.set(15, 1.0f)
    }
}