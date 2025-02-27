package com.heyanle.easy_bangumi_cm

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.heyanle.easy_bangumi_cm.common.theme.EasyTheme
import com.heyanle.easy_bangumi_cm.shared.App
import com.heyanle.easy_bangumi_cm.shared.Nav
import com.heyanle.easy_bangumi_cm.shared.ui.main.Main


/**
 * Created by HeYanLe on 2024/12/3 0:36.
 * https://github.com/heyanLE
 */
// can't run directly, use gradle task
// 不能直接运行，使用 gradle task 启动
fun main(){
    val app = EasyApplication()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "EasyBangumi",
        ) {
            App()
        }
    }
}