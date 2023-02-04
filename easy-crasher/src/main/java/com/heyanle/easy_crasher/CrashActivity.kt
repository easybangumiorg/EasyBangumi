package com.heyanle.easy_crasher

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import org.w3c.dom.Text

/**
 * Created by HeYanLe on 2022/9/4 15:02.
 * https://github.com/heyanLE
 */
class CrashActivity : Activity() {

    companion object {
        const val KEY_ERROR_MSG = "ERROR_MSG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)
        initView()
    }

    private fun initView() {
        val btTitle = findViewById<TextView>(R.id.tv_title)
        val title =
            "手机型号：${android.os.Build.MODEL}\n安卓版本：${android.os.Build.VERSION.RELEASE}"
        btTitle.text = title

        val tvErrMsg = findViewById<TextView>(R.id.tv_msg)


        tvErrMsg.text = intent?.getStringExtra(KEY_ERROR_MSG)
    }

}