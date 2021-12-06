package com.heyanle.easybangumi.ui.detailplay.div

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import cn.jzvd.JzvdStd

import cn.jzvd.JZDataSource
import com.heyanle.easybangumi.EasyApplication
import cn.jzvd.JZUtils

import android.media.AudioManager
import cn.jzvd.Jzvd
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.utils.visible
import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.core.widget.NestedScrollView
import com.heyanle.easybangumi.databinding.PopupLongPressFastBinding

import com.heyanle.easybangumi.utils.getAttrColor
import com.heyanle.easybangumi.utils.gone
import com.heyanle.easybangumi.utils.invisible


/**
 * Created by HeYanLe on 2021/10/6 0:00.
 * https://github.com/heyanLE
 */
class EasyPlayer: JzvdStd {

    init {

        gestureDetector = GestureDetector(context.applicationContext, object: GestureDetector.SimpleOnGestureListener(){
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                if (state == STATE_PLAYING || state == STATE_PAUSE) {
                    Log.d(TAG, "doublClick [" + this.hashCode() + "] ")
                    startButton.performClick()
                }
                return super.onDoubleTap(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                if (!mChangePosition && !mChangeVolume) {
                    onClickUiToggle()
                }
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent?) {
                // TODO : 长按加速

                startFast()
                super.onLongPress(e)
            }

        })
    }



    var onRetry:()->Boolean = {true}

    val episodeList = arrayListOf<String>()

    var onEpisodeClick: (Int)->Unit = {}

    var getCurrentIndex: ()->Int = {0}
    var onSaveLast: (Long)->Unit = {}

    @Volatile var isFast = false

    private val longPress: PopupWindow by lazy {
        PopupWindow(PopupLongPressFastBinding.inflate(LayoutInflater.from(jzvdContext)).root,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            isFocusable = false
            inputMethodMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            isOutsideTouchable = true
            animationStyle = android.R.style.Animation_Translucent
        }
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun setUp(jzDataSource: JZDataSource?, screen: Int) {
        super.setUp(jzDataSource, screen)
        titleTextView.visibility = INVISIBLE
    }

    override fun gotoFullscreen() {
        super.gotoFullscreen()
        titleTextView.visibility = VISIBLE
    }

    override fun gotoNormalScreen() {
        super.gotoNormalScreen()
        titleTextView.visibility = INVISIBLE
        kotlin.runCatching {
            clarityPopWindow.dismiss()
        }
    }

    
    override fun setScreenTiny() {
        super.setScreenTiny()
        Toast.makeText(EasyApplication.INSTANCE, "d", Toast.LENGTH_SHORT).show()
        kotlin.runCatching {
            clarityPopWindow.dismiss()
        }
    }


    override fun setScreenFullscreen() {
        super.setScreenFullscreen()
        clarity.visible()
        clarity.setText(R.string.episode)
        if(state == STATE_PREPARING || state == STATE_ERROR){
            topContainer.visible()
            backButton.visible()
        }
    }

    override fun changeUIToPreparingPlaying() {
        super.changeUIToPreparingPlaying()
        //Log.i("")
    }

    override fun onClickUiToggle() {
        Log.i("EasyPlayer", "ClickUiToggle")
        super.onClickUiToggle()
        clarity.setText(R.string.episode)
        currentTimeTextView.visible()
        totalTimeTextView.visible()
        progressBar.visible()
    }

    override fun changeUIToPreparingChangeUrl() {
        super.changeUIToPreparingChangeUrl()
    }

    override fun onStatePause() {
        super.onStatePause()
        onSaveLast((progressBar.progress*duration)/100)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if(isFast){
            if(event.action == MotionEvent.ACTION_UP){
                stopFast()
            }else
            return true
        }
        val id = v.id
        if (id == cn.jzvd.R.id.surface_container) {
            if(event.action == MotionEvent.ACTION_UP){
                onSaveLast(mSeekTimePosition)
            }
        }



        return super.onTouch(v, event)
    }

    override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        if(screen == SCREEN_FULLSCREEN){
            backButton.visible()
        }
    }

    override fun changeUiToError() {
        super.changeUiToError()
        startButton.gone()
        if(screen == SCREEN_FULLSCREEN){
            topContainer.visible()
            backButton.visible()
        }
        bottomContainer.visible()
        currentTimeTextView.invisible()
        totalTimeTextView.invisible()
        progressBar.invisible()
    }
    override fun changeUiToPreparing() {
        super.changeUiToPreparing()
        if(screen == SCREEN_FULLSCREEN){
            topContainer.visible()
            backButton.visible()
        }
        bottomContainer.visible()
        currentTimeTextView.invisible()
        totalTimeTextView.invisible()
        progressBar.invisible()

    }

    override fun onCompletion() {

        cancelProgressTimer()
        dismissBrightnessDialog()
        dismissProgressDialog()
        dismissVolumeDialog()
        onStateAutoComplete()
        mediaInterface.release()
        JZUtils.saveProgress(context, jzDataSource.currentUrl, 0);
        if (screen == SCREEN_FULLSCREEN) {
            kotlin.runCatching {
                clarityPopWindow.dismiss()
            }
            bottomContainer.visible()
            currentTimeTextView.invisible()
            totalTimeTextView.invisible()
            progressBar.invisible()
            clarity.visible()
        }
        cancelDismissControlViewTimer()

    }

    override fun clickClarity() {
        //super.clickClarity()
        val inflater = LayoutInflater.from(jzvdContext)
        val layout = inflater.inflate(R.layout.popup_layout_clarity, null) as RelativeLayout
        layout.setOnClickListener {
            kotlin.runCatching {
                clarityPopWindow.dismiss()
            }
        }
        val linear = layout.findViewById<LinearLayout>(R.id.video_quality_wrapper_area)
        val mQualityListener = OnClickListener { v1: View ->
            val index = v1.tag as Int
            for (j in 0 until linear.childCount) { //设置点击之后的颜色
                if (j == getCurrentIndex()) {
                    (linear.getChildAt(j) as TextView).setTextColor(getAttrColor(jzvdContext, R.attr.colorSecondary))
                } else {
                    (linear.getChildAt(j) as TextView).setTextColor(Color.WHITE)
                }
            }
            onEpisodeClick(index)
            if (clarityPopWindow != null) {
                clarityPopWindow.dismiss()
            }
        }

        for (j in episodeList.indices) {
            val clarityItem =
                inflate(jzvdContext, cn.jzvd.R.layout.jz_layout_clarity_item, null) as TextView
            clarityItem.text = episodeList[j]
            clarityItem.tag = j
            linear.addView(clarityItem, j)
            clarityItem.setOnClickListener(mQualityListener)
            if (j == getCurrentIndex()) {
                clarityItem.setTextColor(getAttrColor(jzvdContext, R.attr.colorSecondary))
            }
        }
        clarityPopWindow =
            PopupWindow(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, false)
        clarityPopWindow.isFocusable = false
        clarityPopWindow.inputMethodMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        clarityPopWindow.contentView = layout
        clarityPopWindow.isOutsideTouchable = false
        clarityPopWindow.animationStyle = R.style.pop_animation
        clarityPopWindow.setOnDismissListener {
//            JZUtils.hideSystemUI(jzvdContext)
//            JZUtils.hideStatusBar(jzvdContext)
        }
        //JZUtils.hideSystemUI(jzvdContext)
        clarityPopWindow.showAtLocation(textureViewContainer, Gravity.END, 0, 0)
        //onShowClarity()

        //JZUtils.hideSystemUI(jzvdContext) //华为手机和有虚拟键的手机全屏时可隐藏虚拟键 issue:1326

    }


    override fun clickRetryBtn() {
        Log.i("EasyPalyer", "click")
        if(onRetry())
            super.clickRetryBtn()
    }

    override fun gotoNormalCompletion() {
        super.gotoNormalCompletion()

    }

    fun startFast(){
        isFast = true

        runCatching {
            if(state != STATE_PLAYING){
                onStatePlaying()
            }
        }

        runCatching {
            mediaInterface.setSpeed(2F)
            longPress.showAtLocation(findViewById(cn.jzvd.R.id.surface_container), Gravity.TOP or Gravity.CENTER, 0, 0)
        }


    }

    private fun stopFast(){
        runCatching {
            mediaInterface.setSpeed(1F)
            longPress.dismiss()
        }
        isFast = false
    }





}