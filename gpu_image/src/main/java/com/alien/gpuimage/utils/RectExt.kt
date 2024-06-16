package com.alien.gpuimage.utils

import android.graphics.RectF
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Gets a float array of the 2D coordinates representing a rectangles
 * corners.
 * The order of the corners in the float array is:
 * 0------->1
 * ^        |
 * |        |
 * |        v
 * 3<-------2
 *
 * @param r the rectangle to get the corners of
 * @return the float array of corners (8 floats)
 */
fun RectF.getCornersArray(): FloatArray {
    return floatArrayOf(
        this.left, this.top,
        this.right, this.top,
        this.right, this.bottom,
        this.left, this.bottom
    )
}

fun RectF.getCenterArray(): FloatArray {
    return floatArrayOf(this.centerX(), this.centerY())
}

fun FloatArray.trapToRect(): RectF {
    val r = RectF(
        Float.POSITIVE_INFINITY,
        Float.POSITIVE_INFINITY,
        Float.NEGATIVE_INFINITY,
        Float.NEGATIVE_INFINITY
    )
    for (i in 1..this.size step 2) {
        val x = (this[i - 1] * 10).roundToInt() / 10f
        val y = (this[i] * 10).roundToInt() / 10f
        r.left = if (x < r.left) x else r.left
        r.top = if (y < r.top) y else r.top
        r.right = if (x > r.right) x else r.right
        r.bottom = if (y > r.bottom) y else r.bottom
    }
    r.sort()
    return r
}

/**
 * Gets a float array of two lengths representing a rectangles width and height
 * The order of the corners in the input float array is:
 * 0------->1
 * ^        |
 * |        |
 * |        v
 * 3<-------2
 *
 * @param corners the float array of corners (8 floats)
 * @return the float array of width and height (2 floats)
 */
fun FloatArray.getRectSidesFromCorners(): FloatArray {
    return floatArrayOf(
        sqrt(
            (this[0] - this[2]).toDouble()
                .pow(2.0) + (this[1] - this[3]).toDouble().pow(2.0)
        ).toFloat(),
        sqrt(
            (this[2] - this[4]).toDouble().pow(2.0) + (this[3] - this[5]).toDouble()
                .pow(2.0)
        ).toFloat()
    )
}