package com.heyanle.easybangumi4.exo

import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.IScenePlayer

/**
 * Created by heyanlin on 2023/10/31.
 */
class EasyExoPlayer(
    private val innerExoPlayer: ExoPlayer
) : ExoPlayer by innerExoPlayer, IScenePlayer {

    private val scope = MainScope()

    // 上一个使用场景
    @Volatile
    override var scene: String = ""
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
    override fun prepare(scene: String){
        scope.launch {
            "onPrepare  $scene".logi("EasyExoPlayer")
            this@EasyExoPlayer.scene = scene
            innerExoPlayer.prepare()
        }
    }

    override fun stop() {
        scope.launch {
            "stop ${scene}".logi("EasyExoPlayer")
            innerExoPlayer.stop()
        }
    }

    override fun stop(scene: String){
        scope.launch {
            "stopIfScene ${this@EasyExoPlayer.scene} $scene".logi("EasyExoPlayer")
            if(this@EasyExoPlayer.scene == scene){
                innerExoPlayer.stop()
            }
        }

    }


    fun innerPlayer() = innerExoPlayer
}