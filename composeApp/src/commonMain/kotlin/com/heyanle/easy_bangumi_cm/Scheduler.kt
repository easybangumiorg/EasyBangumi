package com.heyanle.easy_bangumi_cm

import com.heyanle.easy_bangumi_cm.base.initBase
import kotlinx.io.files.FileSystem
import kotlinx.io.files.SystemFileSystem
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/11/27 1:08.
 * https://github.com/heyanLE
 */
object Scheduler {

    fun onInit(){
        initBase()

    }

}
