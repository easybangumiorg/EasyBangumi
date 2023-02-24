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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/19 22:41.
 * https://github.com/heyanLE
 */
class ExtensionInstallReceiver (private val listener: Listener):
    BroadcastReceiver(){

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
                getPackageNameFromIntent(intent)?.let {
                    listener.onExtensionInstalled(context, it)
                }
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                getPackageNameFromIntent(intent)?.let {
                    listener.onExtensionUpdated(context, it)
                }
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                getPackageNameFromIntent(intent)?.let {
                    listener.onPackageUninstalled(context, it)
                }
            }
        }
    }

    /**
     * Returns the package name of the installed, updated or removed application.
     */
    private fun getPackageNameFromIntent(intent: Intent?): String? {
        return intent?.data?.encodedSchemeSpecificPart ?: return null
    }


    /**
     * Listener that receives extension installation events.
     */
    interface Listener {
        fun onExtensionInstalled(context: Context, pkgName: String)
        fun onExtensionUpdated(context: Context, pkgName: String)
        fun onPackageUninstalled(context: Context, pkgName: String)
    }
}