package com.heyanle.easybangumi.ui.setting

import android.content.res.Resources.Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.theme.DarkMode
import com.heyanle.easybangumi.theme.EasyThemeController
import com.heyanle.easybangumi.theme.EasyThemeMode
import com.heyanle.easybangumi.theme.getColorScheme
import com.heyanle.easybangumi.ui.common.HomeTopAppBar
import com.heyanle.easybangumi.ui.common.MoeSnackBar
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.easybangumi.ui.common.show
import com.heyanle.easybangumi.utils.stringRes
import com.heyanle.easybangumi.utils.toast

/**
 * Created by HeYanLe on 2023/1/7 22:53.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(){
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            HomeTopAppBar(
                scrollBehavior = scrollBehavior,
                label = {
                    Text(text = stringResource(id = R.string.setting))
                },
            )
        },
        content = {padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
            ) {
                ThemeSettingCard()
            }

        }
    )

}

@Composable
fun ThemeSettingCard(
    modifier: Modifier = Modifier
){
    val themeState by EasyThemeController.easyThemeState
    val isDark = when(themeState.darkMode){
        DarkMode.Dark -> true
        DarkMode.Light -> false
        else -> isSystemInDarkTheme()
    }

    Column(
        modifier = Modifier
            .then(modifier),
    ) {

        Text(
            modifier = Modifier.padding(16.dp, 16.dp),
            text = stringResource(id = R.string.theme),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Start
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EasyThemeMode.values().forEach {
                Box(modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        it.getColorScheme(isDark).secondary
                    )
                    .clickable {
                        EasyThemeController.changeThemeMode(it)
                    }){
                    if(it.name == themeState.themeMode.name){
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(id = R.string.theme),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }

        var darkExpanded by remember {
            mutableStateOf(false)
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                darkExpanded = true
            }
            .padding(16.dp, 16.dp)
            .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if(isDark)Icons.Filled.Brightness2 else Icons.Filled.WbSunny,
                    contentDescription = stringResource(id = R.string.dark_mode) )
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = stringResource(id = R.string.dark_mode))
            }


            Box(){
                val text = when(themeState.darkMode){
                    DarkMode.Dark -> stringResource(id = R.string.dark_on)
                    DarkMode.Light -> stringResource(id = R.string.dark_off)
                    else -> stringResource(id = R.string.dark_auto)
                }
                Row() {
                    Text(
                        modifier = Modifier.alpha(0.6f),
                        text = text)
                    Icon(
                        Icons.Filled.ExpandMore,
                        modifier = Modifier.alpha(0.6f),
                        contentDescription = stringResource(id = R.string.dark_mode) )
                }
                DropdownMenu(
                    expanded = darkExpanded,
                    onDismissRequest = { darkExpanded = false },
                ) {
                    DropdownMenuItem(
                        onClick = {
                            EasyThemeController.changeDarkMode(DarkMode.Dark)
                            darkExpanded = false
                        },
                        text = {
                            Text(text = stringResource(id = R.string.dark_on))
                        }
                    )

                    DropdownMenuItem(
                        onClick = {
                            EasyThemeController.changeDarkMode(DarkMode.Light)
                            darkExpanded = false
                        },
                        text = {
                            Text(text = stringResource(id = R.string.dark_off))
                        }
                    )

                    DropdownMenuItem(
                        onClick = {
                            EasyThemeController.changeDarkMode(DarkMode.Auto)
                            darkExpanded = false
                        },
                        text = {
                            Text(text = stringResource(id = R.string.dark_auto))
                        }
                    )
                }

            }

        }

        if(EasyThemeController.isSupportDynamicColor()){
            var isDynamicCheck by remember {
                mutableStateOf(themeState.isDynamicColor)
            }
            LaunchedEffect(key1 = themeState){
                isDynamicCheck = themeState.isDynamicColor
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val old = isDynamicCheck
                    isDynamicCheck = !old
                    EasyThemeController.changeIsDynamicColor(!old)
                    if (!old) {
                        stringRes(R.string.dynamic_color_enable_msg).moeSnackBar()
                    }
                }
                .padding(16.dp, 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Row (
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        Icons.Filled.ColorLens,
                        contentDescription = stringResource(id = R.string.is_dynamic_color) )
                    Spacer(modifier = Modifier.size(16.dp))
                    Column() {
                        Text(text = stringResource(id = R.string.is_dynamic_color))
                        Text(
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            modifier = Modifier.alpha(0.6f),
                            text = stringResource(id = R.string.is_dynamic_color_msg))
                    }

                }
                Switch(checked = isDynamicCheck, onCheckedChange = {
                    isDynamicCheck = it
                    EasyThemeController.changeIsDynamicColor(it)
                    if(it){
                        stringRes(R.string.dynamic_color_enable_msg).moeSnackBar()
                    }
                })
            }
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                "testtest".moeSnackBar(
                    confirmLabel = "确认",
                    onConfirm = {
                        "确认".toast()
                    }
                )
            }
            .padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(text = "test")
        }



    }
}