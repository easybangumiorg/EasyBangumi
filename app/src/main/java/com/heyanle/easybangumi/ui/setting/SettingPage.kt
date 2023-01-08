package com.heyanle.easybangumi.ui.setting

import android.content.res.Resources.Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Switch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi.theme.DarkMode
import com.heyanle.easybangumi.theme.EasyThemeController
import com.heyanle.easybangumi.theme.EasyThemeMode
import com.heyanle.easybangumi.theme.getColorScheme
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.easybangumi.ui.common.show
import com.heyanle.easybangumi.ui.home.LocalTopAppBarScrollBehavior

/**
 * Created by HeYanLe on 2023/1/7 22:53.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(){
    val scope = rememberCoroutineScope()
    val isDark = when(EasyThemeController.easyThemeState.value.darkMode){
        DarkMode.Dark -> true
        DarkMode.Light -> false
        else -> isSystemInDarkTheme()
    }

    Column() {
        Text(text = "test")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row() {
                EasyThemeMode.values().forEach {
                    Box(modifier = Modifier
                        .size(40.dp)
                        .background(
                            it.getColorScheme(isDark).secondary
                        )
                        .clickable {
                            EasyThemeController.changeThemeMode(it)
                        })
                }
            }
            var isDy by remember {
                mutableStateOf(EasyThemeController.easyThemeState.value.isDynamicColor)
            }
            Switch(checked = isDy, onCheckedChange = {check ->
                isDy = check
                EasyThemeController.changeIsDynamicColor(check)

                moeSnackBar {
                    Text(text = "${if(check)"启动" else "关闭"}桌面动态主题")
                }
            })

        }
    }


}