package org.easybangumi.next.shared.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual fun getDynamicColorAction(): org.easybangumi.next.shared.theme.DynamicColorAction {
    return object: DynamicColorAction {

        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
        override fun support(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        }

        @Composable
        override fun getColorScheme(isDark: Boolean): ColorScheme? {
            val ctx = LocalContext.current
            return if (support()) {
                if (isDark) {
                    androidx.compose.material3.dynamicDarkColorScheme(ctx)
                } else {
                    androidx.compose.material3.dynamicLightColorScheme(ctx)
                }
            } else {
                null
            }
        }
    }
}