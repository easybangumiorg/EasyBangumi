package com.heyanle.easybangumi.player

import com.heyanle.easybangumi.ui.player.AnimPlayViewModel
import com.heyanle.easybangumi.ui.player.BangumiPlayController
import com.heyanle.easybangumi.utils.toast
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.okkv2.core.okkv

/**
 * 小窗状态管理
 * 1. 监听播放界面的 DisposableEffect ，这里加点魔法
 * 2. 监听 Activity 的 onResume 和 onPause
 * Created by HeYanLe on 2023/1/15 19:41.
 * https://github.com/heyanLE
 */
object TinyStatusController {

    var autoTinyEnableOkkv by okkv<Boolean>("AUTO_TINY_ENABLE", def = true)

    private var playerScreenLaunch: Boolean = false

    fun onPlayScreenLaunch(){
        playerScreenLaunch = true
        PlayerTinyController.dismissTiny()
    }

    fun onPlayScreenDispose(){
        playerScreenLaunch = false
        // 如果开启了自动小窗 & 当前视频正在播放 则小窗
        if(autoTinyEnableOkkv && PlayerController.exoPlayer.isPlaying){
            PlayerTinyController.showTiny()
        }else{
            // 如果没开 先暂停再说
            PlayerController.exoPlayer.pause()
        }
    }

    fun onActResume(){
        if(playerScreenLaunch){
            PlayerTinyController.dismissTiny()
            BangumiPlayController.onPlayerScreenReshow()
        }
    }

    fun onActPause(){
        // 如果 当前在播放页 & 开启了自动小窗 & 当前视频正在播放 则小窗
        if(playerScreenLaunch && autoTinyEnableOkkv && PlayerController.exoPlayer.isPlaying){
            PlayerTinyController.showTiny()
        }else{
            // 如果没开 先暂停再说
            PlayerController.exoPlayer.pause()
        }
    }




}