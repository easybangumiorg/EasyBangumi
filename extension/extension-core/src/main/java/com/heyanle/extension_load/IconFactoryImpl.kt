package com.heyanle.extension_load

import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.heyanle.extension_api.ExtensionIconSource
import com.heyanle.extension_api.IconFactory
import com.heyanle.extension_load.model.Extension

/**
 * Created by HeYanLe on 2023/2/22 20:19.
 * https://github.com/heyanLE
 */
class IconFactoryImpl: IconFactory {

    private val iconMap = mutableMapOf<String, Drawable>()

    override fun getIcon(source: ExtensionIconSource): Drawable? {
        if(iconMap.containsKey(source.key)){
            return iconMap[source.key]
        }
        val extension = (ExtensionController.installedExtensionsFlow.value as? ExtensionController.ExtensionState.Extensions) ?: return null
        val ext = extension.extensions.filterIsInstance<Extension.Installed>().find { installed ->
            installed.sources.any {
                it == source
            }
        } ?: return null
        return source.getIconResourcesId()?.let {resId ->
            ext.resources?.let {
                ResourcesCompat.getDrawable(it, resId, null)
            }
        }?.apply {
            iconMap[source.key] = this
        }
    }
}