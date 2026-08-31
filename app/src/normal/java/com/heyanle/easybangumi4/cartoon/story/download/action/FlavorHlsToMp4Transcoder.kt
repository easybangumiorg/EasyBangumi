package com.heyanle.easybangumi4.cartoon.story.download.action

import android.app.Application
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.InAppMp4Muxer
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The Anime4K APK already carries a full media stack, so it keeps the modern Media3 remux path. */
@androidx.annotation.OptIn(UnstableApi::class)
private class Media3HlsToMp4Transcoder(
    private val application: Application,
) : HlsToMp4Transcoder {
    private val scope = MainScope()
    private var transformer: Transformer? = null
    private var progressJob: Job? = null

    override fun start(
        inputPath: String,
        outputPath: String,
        onProgress: (Float) -> Unit,
        onError: (Exception?) -> Unit,
        onCompleted: () -> Unit,
    ) {
        cancel()
        val next = Transformer.Builder(application)
            .setMuxerFactory(InAppMp4Muxer.Factory())
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    finish()
                    onCompleted()
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException,
                ) {
                    finish()
                    onError(exportException)
                }
            })
            .build()
        transformer = next
        next.start(MediaItem.fromUri(inputPath.toUri()), outputPath)
        progressJob = scope.launch {
            val holder = ProgressHolder()
            while (transformer === next) {
                if (next.getProgress(holder) == Transformer.PROGRESS_STATE_AVAILABLE) {
                    onProgress(holder.progress / 100f)
                }
                delay(500)
            }
        }
    }

    override fun cancel() {
        progressJob?.cancel()
        progressJob = null
        transformer?.cancel()
        transformer = null
    }

    private fun finish() {
        progressJob?.cancel()
        progressJob = null
        transformer = null
    }
}

internal fun createFlavorHlsToMp4Transcoder(
    application: Application,
): HlsToMp4Transcoder = Media3HlsToMp4Transcoder(application)
