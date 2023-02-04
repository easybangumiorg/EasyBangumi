package com.heyanle.easybangumi.player

import android.animation.ValueAnimator
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.ui.common.easy_player.TinyEasyPlayerView
import com.heyanle.easybangumi.ui.player.BangumiPlayController
import com.heyanle.easybangumi.utils.OverlayHelper
import com.heyanle.easybangumi.utils.dip2px
import com.heyanle.easybangumi.utils.stringRes
import com.heyanle.easybangumi.utils.toast
import com.heyanle.okkv2.core.okkv
import kotlin.math.absoluteValue


/**
 * Created by HeYanLe on 2023/1/15 19:24.
 * https://github.com/heyanLE
 */
object PlayerTinyController {

    val windowManager by lazy {
        OverlayHelper.getWindowManager(BangumiApp.INSTANCE)
    }

    var tinyWidthDpOkkv by okkv<Int>("TINY_WIDTH_DP", 250)

    val tinyPlayerView: TinyEasyPlayerView by lazy {
        TinyEasyPlayerView(BangumiApp.INSTANCE)
    }

    var isTinyMode = false

    private var layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    fun showTiny() {
        if (OverlayHelper.drawOverlayEnable(BangumiApp.INSTANCE)) {
            val playWhenReady = PlayerController.exoPlayer.playWhenReady
            kotlin.runCatching {
                PlayerController.exoPlayer.pause()
                refreshLayoutParams()
                tinyPlayerView.layoutParams = layoutParams
                kotlin.runCatching {
                    windowManager.addView(
                        tinyPlayerView,
                        layoutParams
                    )
                }.onFailure {
                    it.printStackTrace()
                }
                isTinyMode = true
                tinyPlayerView.post {
                    PlayerController.exoPlayer.playWhenReady = playWhenReady
                    tinyPlayerView.basePlayerView.attachToPlayer(PlayerController.exoPlayer)
                }
                BangumiPlayController.onNewTinyComposeView(tinyPlayerView.basePlayerView)
            }.onFailure { err ->
                isTinyMode = false
                err.printStackTrace()
                kotlin.runCatching {
                    windowManager.removeView(tinyPlayerView)
                }.onFailure {
                    it.printStackTrace()
                }
                err.toast()
            }

        } else {
            isTinyMode = false
            stringRes(R.string.no_overlay_permission).toast()
        }

    }

    fun dismissTiny() {
        isTinyMode = false
        kotlin.runCatching {
            windowManager.removeView(tinyPlayerView)
        }.onFailure {
            it.printStackTrace()
        }
        kotlin.runCatching {
            tinyPlayerView.basePlayerView.detachToPlayer()
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun refreshLayoutParams() {
        layoutParams.type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_PHONE
        else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        layoutParams.width = tinyWidthDpOkkv.dip2px()
        layoutParams.height =
            ((tinyWidthDpOkkv.dip2px() / PlayerController.ratioWidth) * PlayerController.ratioHeight).toInt()
        layoutParams.gravity = Gravity.START or Gravity.TOP
    }


    var isMove = false
    var downWindowX = 0
    var downWindowY = 0
    var downTouchX = 0F
    var downTouchY = 0F

    var downTime = System.currentTimeMillis()

    var slop = ViewConfiguration.get(BangumiApp.INSTANCE).scaledTouchSlop


    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downWindowX = layoutParams.x
                downWindowY = layoutParams.y
                downTouchX = event.rawX
                downTouchY = event.rawY
                downTime = System.currentTimeMillis()
            }

            MotionEvent.ACTION_MOVE -> {
                val nowX = event.rawX
                val nowY = event.rawY
                if (isMove || (nowX - downTouchX).absoluteValue >= slop || (nowY - downTouchY).absoluteValue >= slop) {
                    isMove = true
                    val newWindowX = downWindowX + (nowX - downTouchX)
                    val newWindowY = downWindowY + (nowY - downTouchY)
                    kotlin.runCatching {
                        layoutParams.x = newWindowX.toInt()
                        layoutParams.y = newWindowY.toInt()
                        windowManager.updateViewLayout(tinyPlayerView, layoutParams)
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                moveToEdge()
                if (!isMove && now - downTime < 1000L) {
                    return false
                }
                isMove = false
            }
        }
        return true
    }

    fun moveToEdge() {
        val oldX = layoutParams.x
        val oldY = layoutParams.y

        val newX =
            if (oldX + layoutParams.width / 2F <= getRealWidth() / 2F) 0 else getRealWidth() - layoutParams.width
        val newY = oldY.coerceIn(0, getRealHeight() - layoutParams.height)

        val valueAnim = ValueAnimator.ofFloat(0f, 1f)
        valueAnim.addUpdateListener {
            kotlin.runCatching {
                val i = it.animatedValue as Float
                val x = oldX + (newX - oldX) * i
                val y = oldY + (newY - oldY) * i
                layoutParams.x = x.toInt()
                layoutParams.y = y.toInt()
                windowManager.updateViewLayout(tinyPlayerView, layoutParams)
            }.onFailure {
                it.printStackTrace()
                kotlin.runCatching {
                    valueAnim.cancel()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
        valueAnim.duration = 300
        valueAnim.start()
    }

    fun getRealWidth(): Int {
        val dm = DisplayMetrics()
        windowManager.getDefaultDisplay().getRealMetrics(dm)
        return dm.widthPixels
    }

    fun getRealHeight(): Int {
        val dm = DisplayMetrics()
        windowManager.getDefaultDisplay().getRealMetrics(dm)
        return dm.heightPixels
    }


}