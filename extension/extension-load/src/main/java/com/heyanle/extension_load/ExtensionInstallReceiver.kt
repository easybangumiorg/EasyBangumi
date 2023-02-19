package com.heyanle.extension_load

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.heyanle.extension_load.model.Extension
import com.heyanle.extension_load.model.LoadResult
import com.heyanle.extension_load.utils.loge
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/19 22:41.
 * https://github.com/heyanLE
 */
class ExtensionInstallReceiver (private val listener: Listener):
    BroadcastReceiver(){

    val scope = MainScope()

    /**
     * Registers this broadcast receiver
     */
    fun register(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, Context.RECEIVER_EXPORTED)
        }else{
            context.registerReceiver(this, filter)
        }
    }

    /**
     * Returns the intent filter this receiver should subscribe to.
     */
    private val filter
        get() = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                if (isReplacing(intent)) return
                scope.launch {
                    when (val result = getExtensionFromIntent(context, intent)) {
                        is LoadResult.Success -> listener.onExtensionInstalled(result.extension)
                        else -> {}
                    }
                }
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                scope.launch {
                    when (val result = getExtensionFromIntent(context, intent)) {
                        is LoadResult.Success -> listener.onExtensionUpdated(result.extension)
                        // Not needed as a package can't be upgraded if the signature is different
                        // is LoadResult.Untrusted -> {}
                        else -> {}
                    }
                }
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                if (isReplacing(intent)) return
                val pkgName = getPackageNameFromIntent(intent)
                if (pkgName != null) {
                    listener.onPackageUninstalled(pkgName)
                }
            }
        }
    }

    private suspend fun getExtensionFromIntent(context: Context, intent: Intent?): LoadResult {
        val pkgName = getPackageNameFromIntent(intent)
        if (pkgName == null) {
            "Package name not found".loge()
            return LoadResult.Error
        }
        return scope.async(Dispatchers.IO, CoroutineStart.DEFAULT) { ExtensionLoader.loadExtensionFromPkgName(context, pkgName) }.await()
    }

    /**
     * Returns the package name of the installed, updated or removed application.
     */
    private fun getPackageNameFromIntent(intent: Intent?): String? {
        return intent?.data?.encodedSchemeSpecificPart ?: return null
    }

    /**
     * Returns true if this package is performing an update.
     *
     * @param intent The intent that triggered the event.
     */
    private fun isReplacing(intent: Intent): Boolean {
        return intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
    }

    /**
     * Listener that receives extension installation events.
     */
    interface Listener {
        fun onExtensionInstalled(extension: Extension)
        fun onExtensionUpdated(extension: Extension)
        fun onPackageUninstalled(pkgName: String)
    }
}