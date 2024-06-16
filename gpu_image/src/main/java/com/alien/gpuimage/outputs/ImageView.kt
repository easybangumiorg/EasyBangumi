package com.alien.gpuimage.outputs

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.alien.gpuimage.Framebuffer
import com.alien.gpuimage.RotationMode
import com.alien.gpuimage.Size

class ImageView(context: Context, attrs: AttributeSet) :
    AppCompatImageView(context, attrs, 0), Input {

    private val bitmapView = BitmapView()

    override fun setInputSize(inputSize: Size?, textureIndex: Int) {
        bitmapView.setInputSize(inputSize, textureIndex)
    }

    override fun setInputFramebuffer(framebuffer: Framebuffer?, textureIndex: Int) {
        bitmapView.setInputFramebuffer(framebuffer, textureIndex)
    }

    override fun setInputRotation(inputRotation: RotationMode, textureIndex: Int) {
        bitmapView.setInputRotation(inputRotation, textureIndex)
    }

    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        bitmapView.newFrameReadyAtTime(time, textureIndex)
        post { setImageBitmap(bitmapView.bitmap) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmapView.release()
    }
}