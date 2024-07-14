package com.heyanle.easybangumi4.crash

import android.app.Application
import android.content.Intent
import com.heyanle.easy_crasher.CrashActivity
import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.base.preferences.hekv.HeKVPreference
import com.heyanle.easybangumi4.base.preferences.hekv.HeKVPreferenceStore
import com.heyanle.easybangumi4.source.SourcePreferences
import com.heyanle.easybangumi4.source_api.component.Component
import com.heyanle.easybangumi4.utils.getFilePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Method
import java.util.concurrent.Executors
import android.os.Process
import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.source.SourceConfig
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.utils.loge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Created by heyanle on 2024/5/31.
 * https://github.com/heyanLE
 */
object SourceCrashController {

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private lateinit var application: Application
    private lateinit var folder: File

    var needBlock = false
        private set

    private lateinit var loadingExtensionFile: File
    private lateinit var usingComponentFile: File

    private lateinit var sourcePreferences: SourcePreferences


    fun init(application: Application, sourcePreferences: SourcePreferences){
        this.application = application
        folder = File(application.getFilePath(), "crash")

        this.sourcePreferences = sourcePreferences

        loadingExtensionFile = File(folder, ".loading_extension")
        usingComponentFile = File(folder, ".using_component")

        if (loadingExtensionFile.exists() || usingComponentFile.exists()){
            needBlock = true
            loadingExtensionFile.delete()
            usingComponentFile.delete()
            "检测到崩溃，以启用安全模式，请排除崩溃拓展之后重启".moeDialog()
        }
    }

    fun onExtensionStart(){
        try {
            if (::loadingExtensionFile.isInitialized){
                folder.mkdirs()
                loadingExtensionFile.createNewFile()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun onExtensionEnd(){
        try {
            if (::loadingExtensionFile.isInitialized){
                loadingExtensionFile.delete()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun onComponentStart(){
        try {
            if (::usingComponentFile.isInitialized){
                folder.mkdirs()
                usingComponentFile.createNewFile()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun onComponentEnd(){
        try {
            if (::usingComponentFile.isInitialized){
                usingComponentFile.delete()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }





}