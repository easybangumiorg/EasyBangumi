package com.heyanle.easy_bangumi_cm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.heyanle.easy_bangumi_cm.shared.SharedCompose

/**
 * Created by heyanlin on 2024/12/2.
 */
class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SharedCompose()
        }
    }

}