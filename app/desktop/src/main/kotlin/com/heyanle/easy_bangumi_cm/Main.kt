package com.heyanle.easy_bangumi_cm

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.heyanle.easy_bangumi_cm.base.DesktopLogger
import com.heyanle.easy_bangumi_cm.base.DesktopPathProvider
import com.heyanle.easy_bangumi_cm.base.Logger
import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import com.heyanle.easy_bangumi_cm.base.preference.PreferenceStore
import com.heyanle.easy_bangumi_cm.shared.SharedApp
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import java.lang.management.ManagementFactory


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
            SharedApp.Compose()
        }
    }
}