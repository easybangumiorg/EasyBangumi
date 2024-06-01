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
import com.heyanle.easybangumi4.source.SourceConfig
import com.heyanle.easybangumi4.utils.loge

/**
 * Created by heyanle on 2024/5/31.
 * https://github.com/heyanLE
 */
object SourceCrashController {


    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private lateinit var application: Application
    private lateinit var folder: File
    private lateinit var actionListFile: File
    private lateinit var nativeCrash: File

    private lateinit var sourcePreferences: SourcePreferences



    private val actionList = linkedSetOf<String>()

    fun init(application: Application){
        this.application = application
        folder = File(application.getFilePath(), "crash")
        actionListFile = File(folder, "component_action.txt")
        nativeCrash = File(folder, ".native_crash")
        sourcePreferences = SourcePreferences(HeKVPreferenceStore(HeKV(application.getFilePath(), "global")))
    }


    fun appendComponentAction(key: String, action: String){
//        scope.launch {
//            try {
//                if (!actionListFile.exists()){
//                    folder.mkdirs()
//                    actionListFile.createNewFile()
//                }
//
//                val actionString = "open;${key};${action};"
//                actionList.add("${key};${action}")
//                actionListFile.appendText(actionString + "\n")
//            }catch (e: Throwable){
//                e.printStackTrace()
//            }
//
//        }
    }

    fun appendComponentAction(component: Component, method: Method){
        appendComponentAction(component.source.key, "${component.javaClass.name}#${method.name}")
    }

    fun closeComponentAction(key: String, action: String){
//        scope.launch {
//            try {
//                if (!actionListFile.exists()) {
//                    folder.mkdirs()
//                    actionListFile.createNewFile()
//                }
//                val actionString =
//                    "close;${key};${action};"
//                actionList.remove("${key};${action};")
//                actionListFile.appendText(actionString + "\n")
//            }catch (e: Throwable){
//                e.printStackTrace()
//            }
//        }
    }

    fun closeComponentAction(component: Component, method: Method){
        closeComponentAction(component.source.key, "${component.javaClass.name}#${method.name}")
    }

    fun onJavaCrash(e: Throwable){
        scope.launch {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            e.printStackTrace(printWriter)
            var th: Throwable? = e.cause
            while (th != null) {
                th.printStackTrace(printWriter)
                th = th.cause
            }
            e.printStackTrace()
            val sb = StringBuilder()
            actionList.reversed().forEach {
                val spil = it.split(";")
                val key = spil.getOrNull(1)
                if (key != null){
                    disableSource(key)
                }
                sb.append(it).append("\n")
            }
            jumpToCrashPage("${stringWriter.toString()}")
        }
    }

    fun onNativeCrash(signal: Int, logcat: String){

        scope.launch {
            val k = linkedSetOf<String>()
            if (::actionListFile.isInitialized && actionListFile.exists()){
                for (line in actionListFile.readLines()) {
                    if (line.startsWith("open")){
                        k.add(line.substring(4))
                    }else if(line.startsWith("close")){
                        k.remove(line.substring(5))
                    }
                }
                val sb = StringBuilder()
                val array = k.toTypedArray()
                array.reverse()
                for (i in array.indices) {
                    val it = array[i]
                    val spil = it.split(";")
                    val key = spil.getOrNull(1)
                    if (key != null){
                        disableSource(key)
                    }
                    sb.append(it).append("\n")
                }
                jumpToCrashPage("源操作记录（已关闭相关源）：\n${sb.toString()} \n signal: ${signal} \n ${logcat}")
            }else{
                logcat.loge("Crash")
                jumpToCrashPage("signal: ${signal} \n ${logcat}")
            }


        }
    }

    private fun disableSource(key: String){
        if (::sourcePreferences.isInitialized){
            val map = sourcePreferences.configs.get().toMutableMap()
            val config = map[key]?.copy(enable = false) ?: SourceConfig(
                key,
                Int.MAX_VALUE,
                false
            )
            map[key] = config
            sourcePreferences.configs.set(map)
        }
    }

    private fun jumpToCrashPage(
        error: String
    ){
        runCatching {
            val intent = Intent(application, CrashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra(CrashActivity.KEY_ERROR_MSG, error)
            application.startActivity(intent)
            Process.killProcess(Process.myPid())
            System.exit(0)
        }.onFailure {
            it.printStackTrace()
        }
    }




}