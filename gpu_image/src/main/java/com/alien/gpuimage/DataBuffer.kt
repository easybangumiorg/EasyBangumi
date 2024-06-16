package com.alien.gpuimage

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object DataBuffer {

    /// region 数据转换
    fun intArray(function: (value: IntArray) -> Unit): Int {
        val intArray = IntArray(1)
        function(intArray)
        return intArray[0]
    }

    fun createFloatBuffer(floatArray: FloatArray): FloatBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(floatArray.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val floatBuffer = byteBuffer.asFloatBuffer()
        floatBuffer.put(floatArray)
        floatBuffer.position(0)
        return floatBuffer
    }

    val IMAGE_VERTICES = createFloatBuffer(
        floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
        )
    )

    private val displayNoRotationTextureCoordinates =
        createFloatBuffer(floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f))
    private val displayRotateRightTextureCoordinates =
        createFloatBuffer(floatArrayOf(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f))
    private val displayRotateLeftTextureCoordinates =
        createFloatBuffer(floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f))
    private val displayVerticalFlipTextureCoordinates =
        createFloatBuffer(floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f))
    private val displayHorizontalFlipTextureCoordinates =
        createFloatBuffer(floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f))
    private val displayRotateRightVerticalFlipTextureCoordinates =
        createFloatBuffer(floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f))
    private val displayRotateRightHorizontalFlipTextureCoordinates =
        createFloatBuffer(floatArrayOf(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f))
    private val displayRotate180TextureCoordinates =
        createFloatBuffer(floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f))

    private val noRotationTextureCoordinates =
        createFloatBuffer(floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f))
    private val rotateLeftTextureCoordinates =
        createFloatBuffer(floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f))
    private val rotateRightTextureCoordinates =
        createFloatBuffer(floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f))
    private val verticalFlipTextureCoordinates =
        createFloatBuffer(floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f))
    private val horizontalFlipTextureCoordinates =
        createFloatBuffer(floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f))
    private val rotateRightVerticalFlipTextureCoordinates =
        createFloatBuffer(floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f))
    private val rotateRightHorizontalFlipTextureCoordinates =
        createFloatBuffer(floatArrayOf(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f))
    private val rotate180TextureCoordinates =
        createFloatBuffer(floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f))

    fun textureCoordinatesForRotation(
        rotationMode: RotationMode,
        isDisplayCoordinate: Boolean
    ): FloatBuffer {
        if (isDisplayCoordinate) {
            return when (rotationMode) {
                RotationMode.NoRotation -> displayNoRotationTextureCoordinates
                RotationMode.RotateLeft -> displayRotateLeftTextureCoordinates
                RotationMode.RotateRight -> displayRotateRightTextureCoordinates
                RotationMode.FlipVertical -> displayVerticalFlipTextureCoordinates
                RotationMode.FlipHorizontal -> displayHorizontalFlipTextureCoordinates
                RotationMode.RotateRightFlipVertical -> displayRotateRightVerticalFlipTextureCoordinates
                RotationMode.RotateRightFlipHorizontal -> displayRotateRightHorizontalFlipTextureCoordinates
                RotationMode.Rotate180 -> displayRotate180TextureCoordinates
            }
        } else {
            return when (rotationMode) {
                RotationMode.NoRotation -> noRotationTextureCoordinates
                RotationMode.RotateLeft -> rotateLeftTextureCoordinates
                RotationMode.RotateRight -> rotateRightTextureCoordinates
                RotationMode.FlipVertical -> verticalFlipTextureCoordinates
                RotationMode.FlipHorizontal -> horizontalFlipTextureCoordinates
                RotationMode.RotateRightFlipVertical -> rotateRightVerticalFlipTextureCoordinates
                RotationMode.RotateRightFlipHorizontal -> rotateRightHorizontalFlipTextureCoordinates
                RotationMode.Rotate180 -> rotate180TextureCoordinates
            }
        }
    }

    fun rotationSwapsWidthAndHeight(rotation: RotationMode): Boolean {
        return (rotation == RotationMode.RotateLeft
                || rotation == RotationMode.RotateRight
                || rotation == RotationMode.RotateRightFlipVertical
                || rotation == RotationMode.RotateRightFlipHorizontal)
    }
    /// endregion
}