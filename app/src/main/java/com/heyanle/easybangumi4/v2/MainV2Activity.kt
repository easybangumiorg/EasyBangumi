package com.heyanle.easybangumi4.v2

import androidx.compose.runtime.Composable
import com.heyanle.easybangumi4.MainActivityHost
import com.heyanle.easybangumi4.v2.navigation.V2NavGraph
import com.heyanle.easybangumi4.v2.theme.V2ThemeProvider

/** Main entry point for the V2 presentation layer. */
class MainV2Activity : MainActivityHost() {

    @Composable
    override fun NavigationContent() {
        V2ThemeProvider {
            V2NavGraph()
        }
    }
}
