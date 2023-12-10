package com.heyanle.easybangumi4.extension

import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.heyanle.easybangumi4.case.ExtensionCase
import com.heyanle.extension_api.ExtensionIconSource
import com.heyanle.extension_api.IconFactory
import java.lang.ref.SoftReference

/**
 * Created by heyanlin on 2023/11/1.
 */
class ExtensionIconFactoryImpl(
    private val extensionCase: ExtensionCase
) : IconFactory {

    private val iconMap = mutableMapOf<String, SoftReference<Drawable>>()

    override fun getIcon(source: ExtensionIconSource): Drawable? {
        val old = iconMap.get(source.key)?.get()
        if (old != null) {
            return old
        }
        val state = extensionCase.flowExtensionState().value
        val extension =
            (state.appExtensions.values + state.fileExtension.values).filterIsInstance<Extension.Installed>()
                .find {
                    it.sources.find { it.key == source.key } != null
                } ?: return null
        return source.getIconResourcesId()?.let { resId ->
            extension.resources?.let {
                ResourcesCompat.getDrawable(it, resId, null)
            }
        }?.apply {
            iconMap[source.key] = SoftReference(this)
        }
    }
}