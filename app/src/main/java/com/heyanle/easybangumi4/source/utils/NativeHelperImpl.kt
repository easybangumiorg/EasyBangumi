package com.heyanle.easybangumi4.source.utils

import android.content.Context
import android.os.Build
import com.heyanle.easybangumi4.extension.service.NativeLoadService
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.getInnerFilePath
import com.heyanle.extension_api.Extension
import com.heyanle.extension_api.NativeHelper
import com.heyanle.extension_api.nativeHelper
import dalvik.system.PathClassLoader
import java.io.File

/**
 * Created by heyanle on 2024/6/2.
 * https://github.com/heyanLE
 */
class NativeHelperImpl(
    private val context: Context
): NativeHelper {

    private val nativeFolder = File(context.getInnerFilePath("native"))
    private val loadedSoPath: HashSet<String> = hashSetOf()


    init {
        nativeHelper = this
        nativeFolder.deleteRecursively()
        nativeFolder.mkdirs()
    }

    override fun tryLoadNativeLib(extension: Extension, libName: String): Boolean {
        try {
            val libFile = File(extension.bundle?.libPath ?: return false)
            for (supportedAbi in Build.SUPPORTED_ABIS) {
                if (supportedAbi == null) {
                    continue
                }
                val abiRoot = File(libFile, supportedAbi)
                val soFile = File(abiRoot, "lib${libName}.so")
                if (!soFile.exists()) {
                    continue
                }

                if (loadedSoPath.contains(soFile.absolutePath)){
                    "检测到插件尝试重复加载 Native 库，建议重启刷新".moeDialog()
                    return true
                }

                val newFile = File(nativeFolder, "${System.currentTimeMillis()}${soFile.name}")
                nativeFolder.mkdirs()
                newFile.delete()
                soFile.copyTo(newFile)
                newFile.deleteOnExit()
                //extension.load(newFile.absolutePath)
                NativeLoadService(extension.javaClass.classLoader ?: return false).load(newFile.absolutePath)
                loadedSoPath.add(soFile.absolutePath)
                return true
            }

        }catch (e: Exception){
            e.printStackTrace()
        }

        return false
    }


//    override fun tryLoad(path: String, libName: String): Boolean {

//
//            try {
//                nativeFolder.mkdirs()
//                val newFile = File(nativeFolder, "${System.currentTimeMillis()}" + soFile.name)
//                soFile.copyTo(newFile)
//
//                System.load(newFile.absolutePath)
//                loadedSoPath.add(soFile.absolutePath)
//                return true
//            }catch (e: Exception){
//                e.printStackTrace()
//                e.message?.moeDialog()
//                return false
//            }
//
//            break
//        }
//        return false
//    }
}