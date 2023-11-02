package com.heyanle.easybangumi4.exo

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource

/**
 * Created by heyanlin on 2023/10/31.
 */
class EasyExoPlayer(
    private val innerExoPlayer: ExoPlayer
) : ExoPlayer by innerExoPlayer {

    // 上一个使用场景
    var scene: String = ""
        private set

    @Deprecated("should Call prepare(scene: String)",
        ReplaceWith("prepare(scene: String)")
    )
    override fun prepare() {
        throw IllegalAccessException("should Call prepare(scene: String)")
    }

    /**
     * 强制指定场景，业务侧如果获取到 scene 改变可以判断为被其他业务使用过
     * 则需要重新加载 media
     * @param scene 使用场景
     */
    fun prepare(scene: String){
        this.scene = scene
        innerExoPlayer.prepare()
    }


    fun innerPlayer() = innerExoPlayer
}