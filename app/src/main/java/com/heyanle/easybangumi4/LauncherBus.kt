package com.heyanle.easybangumi4

import android.app.Activity
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.heyanle.easybangumi4.utils.CoroutineProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.suspendCoroutine

/**
 * Created by heyanlin on 2024/5/21.
 */
class LauncherBus(
    act: ComponentActivity
) {

    companion object {
        private var _current: WeakReference<LauncherBus>? = null
        val current: LauncherBus?
            get() = _current?.get()
        fun onResume(launcherBus: LauncherBus) {
            _current = WeakReference(launcherBus)
        }
    }

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // 1. 选择备份文件
    private var getBackupZipCallback: WeakReference<(Uri?)->Unit>? = null
    private val getBackupZipLauncher = act.registerForActivityResult(ActivityResultContracts.GetContent()){
        scope.launch {
            getBackupZipCallback?.get()?.invoke(it)
            getBackupZipCallback = null
        }
    }
    fun getBackupZip(callback: (Uri?)->Unit){
        scope.launch {
            if (getBackupZipCallback != null){
                getBackupZipCallback?.get()?.invoke(null)
            }
            getBackupZipCallback = WeakReference(callback)
            getBackupZipLauncher.launch("application/zip")
        }
    }


}