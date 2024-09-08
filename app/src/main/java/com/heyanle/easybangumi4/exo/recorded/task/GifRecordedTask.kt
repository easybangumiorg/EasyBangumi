package com.heyanle.easybangumi4.exo.recorded.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.graphics.transform
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.transformer.ExportException
import com.alien.gpuimage.filter.CropFilter
import com.alien.gpuimage.filter.DropFrameFilter
import com.alien.gpuimage.filter.TransformFilter
import com.alien.gpuimage.outputs.BitmapOut
import com.heyanle.easy_transformer.transform.EasyTransform
import com.heyanle.easybangumi4.exo.ClippingConfigMediaSourceFactory
import com.heyanle.easybangumi4.utils.AnimatedGifEncoder
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.util.PriorityQueue
import kotlin.Exception

/**
 * Created by heyanlin on 2024/6/24.
 */
@OptIn(UnstableApi::class)
class GifRecordedTask(
    private val ctx: Context,
    private val mediaItem: MediaItem,
    private val mediaSourceFactory: MediaSource.Factory,

    private val outputFolder: File,
    private val outputName: String,

    private val startPosition: Long,
    private val endPosition: Long,

    // x 轴和 y 轴都是 [0,1]，该矩形表示裁剪范围占各个边的比例
    // y 轴正方向向下
    private val crop: RectF,
    private val fps: Int,
    private val quality: Int,
) : AbsRecordedTask(), BitmapOut.BitmapOutCallback, EasyTransform.OnTransformListener {

    private val maxStepCount = 2


    private val priorityQueue = PriorityQueue<Pair<Long, File>>(
        11, compareBy { it.first }
    )
    private val frameTempFolder = File(outputFolder, "${outputName}.temp")
    private val file = File(outputFolder, outputName)


    // 生成图片
    private val bitmapOut = BitmapOut().apply {
        callback = this@GifRecordedTask
    }

    private var firstTime = -1L
    override fun onBitmapAvailable(bitmap: Bitmap?, time: Long?) {
        bitmap ?: return
        time ?: return
        val cop = bitmap.copy(bitmap.config, false)
        singleScope.launch {
            if (frameTempFolder.isFile){
                frameTempFolder.deleteRecursively()
            }
            frameTempFolder.mkdirs()
            val frameFile = File(frameTempFolder, "${time}.png")
            frameFile.delete()
            frameFile.createNewFile()
            cop.compress(Bitmap.CompressFormat.PNG, 100, frameFile.outputStream())
            cop.recycle()
            priorityQueue.add(Pair(time, frameFile))
            Log.i("GifRecordedTask", "onBitmapAvailable: ${frameFile.absolutePath}")

            if (firstTime == -1L){
                firstTime = time
            }
            val duringMs = endPosition - startPosition
            val currentMs = (time - firstTime)/1000000
            dispatchProcess((currentMs*100/duringMs).toInt(), "加载帧图中...", 0)
        }

    }

    // 裁剪，需要将上一步缩放导致的黑边也裁掉
    private val cropFilter = CropFilter().apply {
        val matrix = Matrix()
        matrix.postScale(quality/100F, quality/100F, 0.5f, 0.5f)
        setCropRegion(crop.transform(matrix))
        addTarget(bitmapOut)
    }

    // 缩放，这里不会缩放画布
    private val transformFilter = TransformFilter().apply {
        addTarget(cropFilter)
        val mat = FloatArray(16){0F}
        mat[0] = quality/100F
        mat[5] = quality/100F
        mat[10] = quality/100F
        mat[15] = 1F
        setTransform3D(mat)
    }
    // 丢帧
    private val dropFrameFilter = DropFrameFilter(fps.toFloat()).apply {
        addTarget(transformFilter)
    }
    private val easyTransformation = EasyTransform(
        ctx,
        mediaItem,
        ClippingConfigMediaSourceFactory(
            mediaSourceFactory,
            MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(startPosition)
                .setEndPositionMs(endPosition)
                .setStartsAtKeyFrame(false)
                .build()
        ),
        dropFrameFilter
    ).apply {
        listener = this@GifRecordedTask
    }


    @Volatile
    private var isTransformCompletely = false
    override fun onTransformCompletely() {
        if (isTransformCompletely) {
            return
        }
        isTransformCompletely = true
        Log.i("GifRecordedTask", "onTransformCompletely:1 ")
        singleScope.launch {
            Log.i("GifRecordedTask", "onTransformCompletely:2 ")
            innerStarTransformGif()
        }

    }

    private suspend fun innerStarTransformGif(){
        try {

            var lastDelay = -1L
            var lastTime = -1L

            val encoder = AnimatedGifEncoder()
            outputFolder.mkdirs()
            file.delete()
            file.createNewFile()
            val stream = BufferedOutputStream(file.outputStream())
            encoder.start(stream)
            encoder.setRepeat(0)
            val all = priorityQueue.size
            var lastBmp: Bitmap? = null

            while (priorityQueue.isNotEmpty()){
                val pair = priorityQueue.poll() ?: continue

                pair.second.absolutePath.logi("GifRecordedTask")
                val curBmp = BitmapFactory.decodeFile(pair.second.absolutePath)
                val currentTime = pair.first

                if (lastBmp == null || lastTime == -1L){
                    lastBmp = curBmp
                    lastTime = currentTime
                    continue
                }

                val time = currentTime - lastTime
                lastDelay = time/1000000
                "${time.toInt()/1000000}".logi("GifRecordedTask")
                encoder.setDelay(time.toInt()/1000000)
                encoder.addFrame(lastBmp)
                stream.flush()
                lastBmp.recycle()
                lastBmp = curBmp
                lastTime = currentTime

                dispatchProcess(100*(all-priorityQueue.size)/all, "生成Gif中...", 1)
            }
            if (lastBmp != null && lastTime != -1L){
                val delay = if (lastDelay == -1L) 1000/fps else lastDelay
                encoder.setDelay(delay.toInt())
                encoder.addFrame(lastBmp)
            }
            lastBmp?.recycle()

            encoder.finish()
            //frameTempFolder.deleteRecursively()
            dispatchCompletely(file)
        }catch (e: Exception){
            e.printStackTrace()
            dispatchError(e, e.message)
        }
    }
    override fun onTransformFailed(exportException: ExportException) {
        dispatchError(exportException, exportException.message)
    }

    private fun dispatchProcess(progress: Int, status: String, step: Int){
        val weight = (1f/maxStepCount)
        val cur = weight * progress + step * 100 * weight
        dispatchProcess(cur.toInt(), status)

    }




    // 外部接口 ===========================
    override fun start() {
        if (state.value.status != 0){
            return
        }

        singleScope.launch {
            try {
                outputFolder.mkdirs()
                frameTempFolder.delete()
                easyTransformation.start()
            }catch (e: Exception){
                e.printStackTrace()
                dispatchError(e, e.message)
            }
        }
    }

    override fun stop() {

        mainScope.launch {
            easyTransformation.cancel()
            release()
            bitmapOut.release()
            cropFilter.release()
            transformFilter.release()
            dropFrameFilter.release()
        }

    }


}