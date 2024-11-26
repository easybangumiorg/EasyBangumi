package com.heyanle.easybangumi4.splash

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.LocalWindowSizeController
import com.heyanle.easybangumi4.MainActivity
import com.heyanle.easybangumi4.Migrate
import com.heyanle.easybangumi4.Scheduler
import com.heyanle.easybangumi4.theme.EasyTheme
import com.heyanle.easybangumi4.theme.NormalSystemBarColor
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.utils.MediaUtils
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import com.heyanle.okkv2.core.okkv
import java.lang.ref.WeakReference

/**
 * 闪屏页
 * Created by heyanlin on 2024/7/4.
 */

val LocalSplashActivity = staticCompositionLocalOf<SplashActivity> {
    error("SplashActivity Not Provide")
}

class SplashActivity : ComponentActivity() {

    companion object {
        const val TAG = "SplashActivity"
        var splashCompletely = false
        var lastSplashActivity: WeakReference<SplashActivity>? = null

    }
    val splashGuildController = Inject.get<SplashGuildController>()
    var first by okkv("first_visible_splash", def = true)
    private val launcherBus = LauncherBus(this)
    override fun onResume() {
        super.onResume()
        LauncherBus.onResume(launcherBus)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lat = lastSplashActivity?.get()
        if (lat != this){
            lat?.finish()
            lastSplashActivity = WeakReference(this)
        }
        if (splashCompletely) {
            jumpToMain()
        } else {


            MediaUtils.setIsDecorFitsSystemWindows(this, false)
            Scheduler.runOnSplashActivityCreate(this, first)
            first = false
            if (splashGuildController.realStep.isEmpty()) {
                jumpToMain()
                return
            }
            setContentView(FrameLayout(this))
            setContent {
                val isMigrating by Migrate.isMigrating.collectAsState()
                val windowClazz = calculateWindowSizeClass(this)
                LaunchedEffect(key1 = Unit) {

                }
                CompositionLocalProvider(
                    LocalWindowSizeController provides windowClazz,
                    LocalSplashActivity provides this,
                ) {
                    EasyTheme {
                        NormalSystemBarColor()
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .systemBarsPadding()
                            ) {
                                if (isMigrating) {
                                    LoadingPage(
                                        modifier = Modifier.fillMaxSize(),
                                        loadingMsg = stringResource(id = R.string.migrating)
                                    )
                                } else {
                                    Splash()
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    fun jumpToMain(){

        val con: SplashGuildController by Inject.injectLazy()
        con.end()

        splashCompletely = true
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}