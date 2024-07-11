package loli.ball.easyplayer2

import android.transition.Scene

interface IScenePlayer {

    val scene: String


    fun prepare(scene: String)

    fun stop(scene: String)
}