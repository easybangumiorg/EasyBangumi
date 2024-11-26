package com.heyanle.easybangumi4.crash

import android.app.Application
import com.heyanle.easybangumi4.plugin.source.SourcePreferences
import com.heyanle.easybangumi4.utils.getFilePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.io.File
import java.util.concurrent.Executors
import com.heyanle.easybangumi4.ui.common.moeDialogAlert

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
            "检测到崩溃，以启用安全模式，请排除崩溃拓展之后重启".moeDialogAlert()
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