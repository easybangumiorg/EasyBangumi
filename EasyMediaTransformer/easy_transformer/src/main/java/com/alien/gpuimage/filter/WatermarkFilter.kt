package com.alien.gpuimage.filter

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.RectF
import android.opengl.GLES20
import com.alien.gpuimage.DataBuffer
import com.alien.gpuimage.sources.Picture
import java.nio.FloatBuffer

/**
 * 水印功能，可以添加 bitmap 任何东西
 */
class WatermarkFilter(bitmap: Bitmap) : Filter() {

    private var picture: Picture? = null

    private var position: Point = Point(0, 0)
    private var watermarkRect: RectF = RectF()
    private var windowVertices: FloatArray = FloatArray(8)
    private var windowVerticesBuffer: FloatBuffer = DataBuffer.createFloatBuffer(windowVertices)

    init {
        picture = Picture(bitmap, false)
        picture?.processPicture()
    }

    override fun renderToTexture(vertices: FloatBuffer, textureCoordinates: FloatBuffer) {
        super.renderToTexture(vertices, textureCoordinates)

        val width = outputFramebuffer?.width ?: 0
        val height = outputFramebuffer?.height ?: 0

        watermarkRect.left = position.x.toFloat() / width.toFloat()
        watermarkRect.top = position.y.toFloat() / height.toFloat()
        watermarkRect.right =
            (position.x + (picture?.outputFramebuffer?.width ?: 0)) / width.toFloat()
        watermarkRect.bottom =
            (position.y + (picture?.outputFramebuffer?.height ?: 0)) / height.toFloat()

        windowVertices[0] = (watermarkRect.left * 2f) - 1f
        windowVertices[1] = (watermarkRect.top * 2f) - 1f
        windowVertices[2] = ((watermarkRect.left + watermarkRect.width()) * 2f) - 1f
        windowVertices[3] = (watermarkRect.top * 2f) - 1
        windowVertices[4] = (watermarkRect.left * 2f) - 1
        windowVertices[5] = ((watermarkRect.top + watermarkRect.height()) * 2f) - 1f
        windowVertices[6] = ((watermarkRect.left + watermarkRect.width()) * 2f) - 1f
        windowVertices[7] = ((watermarkRect.top + watermarkRect.height()) * 2f) - 1f

        windowVerticesBuffer.rewind()
        windowVerticesBuffer.put(windowVertices)
        windowVerticesBuffer.position(0)

        // 开启混合
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, picture?.outputFramebuffer?.textureId ?: 0)
        GLES20.glUniform1i(inputImageTextureUniform, 3)

        // GL坐标
        GLES20.glEnableVertexAttribArray(positionAttribute)
        GLES20.glVertexAttribPointer(
            positionAttribute,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            windowVerticesBuffer
        )

        // 纹理坐标
        GLES20.glEnableVertexAttribArray(inputTextureCoordinateAttribute)
        GLES20.glVertexAttribPointer(
            inputTextureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 0, textureCoordinates
        )

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // 关闭 属性 混合
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glDisableVertexAttribArray(positionAttribute)
        GLES20.glDisableVertexAttribArray(inputTextureCoordinateAttribute)
    }

    override fun release() {
        super.release()
        runSynchronously(Runnable {
            picture?.release()
            picture = null
        })
    }

    /**
     * 设置文字位置
     */
    fun setPosition(x: Int, y: Int) {
        this.position.x = x
        this.position.y = y
    }
}