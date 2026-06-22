package com.alien.gpuimage.utils;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import androidx.annotation.Keep;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * OpenGL 工具类。
 */
@Keep
public class GLUtils {

    public static Bitmap readTextureToBitmap(int texture, int width, int height) {
        int[] fbo = new int[1];
        GLES20.glGenFramebuffers(1, fbo, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture, 0);

        int fboStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (fboStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Logger.e("GLUtils", "initFBO failed, status: " + fboStatus);
        }
        Bitmap bmp = readFboToBitmap(fbo[0], width, height);
        GLES20.glDeleteFramebuffers(1, fbo, 0);
        return bmp;
    }

    /**
     * @param fbo    读取fbo后转Bitmap
     * @param width  fbo width
     * @param height fbo height
     * @return 返回Bitmap数据
     */
    public static Bitmap readFboToBitmap(int fbo, int width, int height) {
        ByteBuffer byteBuffer;
        byteBuffer = ByteBuffer.allocateDirect(width * height * 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.rewind();
        byteBuffer.position(0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
        return rgbaBufferToBitmap(byteBuffer, width, height);
    }

    public static Bitmap rgbaBufferToBitmap(Buffer buffer, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }

    public static Bitmap rgbaToBitmap(ByteBuffer data, int stride, int width, int height) {
        byte[] srcData;
        if (data.isDirect()) {
            srcData = new byte[stride * height];
            data.get(srcData);
        } else {
            srcData = data.array();
        }

        int[] colors = convertRgbaByteToColor(srcData, stride, width, height);
        return Bitmap.createBitmap(colors, 0, width, width, height, Bitmap.Config.ARGB_8888);
    }

    private static int[] convertRgbaByteToColor(byte[] data, int stride, int width, int height) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int red, green, blue, alpha;
        int colorOffset = 4;

        // 逐行逐列读取，忽略大于宽度部分数据
        int[] color = new int[width * height];
        for (int row = 0; row < height; ++row) {
            int srcRowIndex = row * stride;
            int dstRowIndex = row * width;
            for (int column = 0; column < width; ++column) {
                red = convertByteToInt(data[column * colorOffset + srcRowIndex]);
                green = convertByteToInt(data[column * colorOffset + 1 + srcRowIndex]);
                blue = convertByteToInt(data[column * colorOffset + 2 + srcRowIndex]);
                alpha = convertByteToInt(data[column * colorOffset + 3 + srcRowIndex]);
                color[column + dstRowIndex] = (red << 16) | (green << 8) | blue | (alpha << 24);
            }
        }

        return color;
    }

    private static int convertByteToInt(byte data) {
        int heightBit = (data >> 4) & 0x0F;
        int lowBit = 0x0F & data;
        return heightBit * 16 + lowBit;
    }
}
