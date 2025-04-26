package org.easybangumi.next.shared.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

actual fun getDynamicColorAction(): org.easybangumi.next.shared.theme.DynamicColorAction {
    return object: DynamicColorAction {
        override fun support(): Boolean {
            return false
        }

        @Composable
        override fun getColorScheme(isDark: Boolean): ColorScheme? {
            return null
        }
    }
}