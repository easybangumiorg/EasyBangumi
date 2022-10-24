package com.heyanle.easy_player.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo

/**
 * 将 IComponentContainer 和 IPlayerController 组合
 * @see IComponentContainer
 * @see IPlayerController
 * Created by HeYanLe on 2022/10/23 16:59.
 * https://github.com/heyanLE
 */
class ControllerWrapper(
    private val componentContainer: IComponentContainer,
    private val playerController: IPlayerController,
): IComponentContainer by componentContainer, IPlayerController by playerController {

    fun togglePlay(){
        if(isPlaying()){
            pause()
        }else{
            start()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun toggleFullScreen(activity: Activity){
        if(activity.isFinishing){
            return
        }
        if(isFullScreen()){
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            stopFullScreen()
        }else{
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            startFullScreen()
        }
    }

    fun toggleLockState(){
        setLocked(!isLocked())
    }

    fun toggleShowState(){
        if(!isShowing()){
            hide()
        }else{
            show()
        }
    }

}