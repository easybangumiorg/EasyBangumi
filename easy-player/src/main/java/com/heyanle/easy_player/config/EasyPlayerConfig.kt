package com.heyanle.easy_player.config

import com.heyanle.easy_player.constant.ScreenScaleType
import com.heyanle.easy_player.player.IEasyPlayerFactory
import com.heyanle.easy_player.player.LruMemoryProgressManager
import com.heyanle.easy_player.player.ProgressManager
import com.heyanle.easy_player.player.exo.ExoPlayerFactory
import com.heyanle.easy_player.render.IRenderFactory
import com.heyanle.easy_player.render.SurfaceRenderFactory

/**
 * Created by HeYanLe on 2022/10/23 15:04.
 * https://github.com/heyanLE
 */
class EasyPlayerConfig (
    val playOnMobileNetwork: Boolean,
    val isOrientationEnable: Boolean,
    val isAudioFocusEnable: Boolean,
    val isCutoutAdapt: Boolean,
    val screenScaleType: Int,
    val isEnablePlayerLog: Boolean,
    val progressManager: ProgressManager,
    val playerFactory: IEasyPlayerFactory,
    val renderFactory: IRenderFactory,
){

    class Builder(){
        var playOnMobileNetwork: Boolean = true
        var isOrientationEnable: Boolean = true
        var isAudioFocusEnable: Boolean = true
        var isCutoutAdapt: Boolean = false
        var screenScaleType: Int = ScreenScaleType.SCREEN_SCALE_DEFAULT
        val isEnablePlayerLog: Boolean = true
        var progressManager: ProgressManager = LruMemoryProgressManager()
        var playerFactory: IEasyPlayerFactory = ExoPlayerFactory()
        var renderFactory: IRenderFactory = SurfaceRenderFactory()

        fun build(): EasyPlayerConfig {
            return EasyPlayerConfig(
                playOnMobileNetwork,
                isOrientationEnable,
                isAudioFocusEnable,
                isCutoutAdapt,
                screenScaleType,
                isEnablePlayerLog,
                progressManager,
                playerFactory,
                renderFactory,
            )
        }
    }


}