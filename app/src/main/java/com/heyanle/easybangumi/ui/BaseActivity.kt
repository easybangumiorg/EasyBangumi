package com.heyanle.easybangumi.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.heyanle.easybangumi.utils.DarkUtils

/**
 * Created by HeYanLe on 2021/9/20 15:23.
 * https://github.com/heyanLE
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DarkUtils.theme(this)
    }

    override fun onDestroy() {
        DarkUtils.destroy(this)
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                DarkUtils.auto(this)
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                DarkUtils.auto(this)
            } // Night mode is active, we're using dark theme
        }
    }



}