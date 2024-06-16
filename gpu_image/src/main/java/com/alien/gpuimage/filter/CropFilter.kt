package com.alien.gpuimage.filter

import android.graphics.RectF
import com.alien.gpuimage.DataBuffer
import com.alien.gpuimage.RotationMode
import com.alien.gpuimage.Size
import java.nio.FloatBuffer
import kotlin.math.roundToInt

/**
 * 裁剪滤镜
 */
class CropFilter : Filter() {

    private val cropRegion = RectF(0f, 0f, 1f, 1f)
    private val originallySuppliedInputSize: Size = Size()
    private val cropTextureCoordinates: FloatBuffer =
        DataBuffer.createFloatBuffer(floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f))
    private var vertices = DataBuffer.IMAGE_VERTICES

    override fun setInputSize(inputSize: Size?, textureIndex: Int) {
        val rotatedSize = rotatedSize(inputSize!!, textureIndex)
        originallySuppliedInputSize.setSize(rotatedSize)

        val scaledSize = Size()
        scaledSize.width = (rotatedSize.width * cropRegion.width()).roundToInt()
        scaledSize.height = (rotatedSize.height * cropRegion.height()).roundToInt()
        if (scaledSize.width == 0 && scaledSize.height == 0) {
            super.setInputSize(scaledSize, textureIndex)
        } else if (!(getInputSize()?.width == scaledSize.width
                    && getInputSize()?.height == scaledSize.height)
        ) {
            super.setInputSize(scaledSize, textureIndex)
        }
    }

    override fun setInputRotation(rotation: RotationMode, textureIndex: Int) {
        super.setInputRotation(rotation, textureIndex)
        calculateCropTextureCoordinates()
    }

    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        renderToTexture(vertices, cropTextureCoordinates)
        informTargetsAboutNewFrameAtTime(time)
    }

    /**
     * 设置裁剪框
     */
    fun setCropRegion(rectF: RectF) {
        check(
            rectF.left in 0.0..1.0 && rectF.top in 0.0..1.0 &&
                    rectF.right in 0.0..1.0 && rectF.bottom in 0.0..1.0
        )

        cropRegion.set(rectF)
        vertices = DataBuffer.IMAGE_VERTICES
        calculateCropTextureCoordinates()
    }

    /**
     * 设置裁剪框
     */
    fun setCropRegion(floatArray: FloatArray, rectF: RectF) {
        setCropRegion(rectF)
        vertices = DataBuffer.createFloatBuffer(floatArray)
    }

    private fun calculateCropTextureCoordinates() {
        val minX = cropRegion.left
        val minY = cropRegion.top
        val maxX = cropRegion.right
        val maxY = cropRegion.bottom
        val buf = FloatArray(8)

        when (getInputRotation()) {
            RotationMode.NoRotation -> {
                buf[0] = minX // 0,0
                buf[1] = minY
                buf[2] = maxX // 1,0
                buf[3] = minY
                buf[4] = minX // 0,1
                buf[5] = maxY
                buf[6] = maxX // 1,1
                buf[7] = maxY
            }
            RotationMode.RotateLeft -> {
                buf[0] = maxY // 1,0
                buf[1] = 1.0f - maxX
                buf[2] = maxY // 1,1
                buf[3] = 1.0f - minX
                buf[4] = minY // 0,0
                buf[5] = 1.0f - maxX
                buf[6] = minY // 0,1
                buf[7] = 1.0f - minX
            }
            RotationMode.RotateRight -> {
                buf[0] = minY // 0,1
                buf[1] = 1.0f - minX
                buf[2] = minY // 0,0
                buf[3] = 1.0f - maxX
                buf[4] = maxY // 1,1
                buf[5] = 1.0f - minX
                buf[6] = maxY // 1,0
                buf[7] = 1.0f - maxX
            }
            RotationMode.FlipVertical -> {
                buf[0] = minX // 0,1
                buf[1] = maxY
                buf[2] = maxX // 1,1
                buf[3] = maxY
                buf[4] = minX // 0,0
                buf[5] = minY
                buf[6] = maxX // 1,0
                buf[7] = minY
            }
            RotationMode.FlipHorizontal -> {
                buf[0] = maxX // 1,0
                buf[1] = minY
                buf[2] = minX // 0,0
                buf[3] = minY
                buf[4] = maxX // 1,1
                buf[5] = maxY
                buf[6] = minX // 0,1
                buf[7] = maxY
            }
            RotationMode.Rotate180 -> {
                buf[0] = maxX // 1,1
                buf[1] = maxY
                buf[2] = minX // 0,1
                buf[3] = maxY
                buf[4] = maxX // 1,0
                buf[5] = minY
                buf[6] = minX // 0,0
                buf[7] = minY
            }
            RotationMode.RotateRightFlipVertical -> {
                buf[0] = minY // 0,0
                buf[1] = 1.0f - maxX
                buf[2] = minY // 0,1
                buf[3] = 1.0f - minX
                buf[4] = maxY // 1,0
                buf[5] = 1.0f - maxX
                buf[6] = maxY // 1,1
                buf[7] = 1.0f - minX
            }
            RotationMode.RotateRightFlipHorizontal -> {
                buf[0] = maxY // 1,1
                buf[1] = 1.0f - minX
                buf[2] = maxY // 1,0
                buf[3] = 1.0f - maxX
                buf[4] = minY // 0,1
                buf[5] = 1.0f - minX
                buf[6] = minY // 0,0
                buf[7] = 1.0f - maxX
            }
        }
        cropTextureCoordinates.put(buf)
        cropTextureCoordinates.rewind()
    }
}