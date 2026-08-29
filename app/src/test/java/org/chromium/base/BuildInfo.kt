package org.chromium.base

import com.heyanle.easybangumi4.utils.WebViewPackageNameScope

/** Test fixture whose class and method names match Chromium's package-name lookup. */
internal object BuildInfo {

    fun getAll(): Boolean = WebViewPackageNameScope.shouldSpoofPackageName()
}
