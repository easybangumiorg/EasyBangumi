package com.heyanle.easybangumi.ui.home.setting

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi.BuildConfig
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.player.TinyStatusController
import com.heyanle.easybangumi.theme.DarkMode
import com.heyanle.easybangumi.theme.EasyThemeController
import com.heyanle.easybangumi.theme.EasyThemeMode
import com.heyanle.easybangumi.theme.getColorScheme
import com.heyanle.easybangumi.ui.common.OkImage
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.easybangumi.utils.OverlayHelper
import com.heyanle.easybangumi.utils.stringRes
import com.heyanle.easybangumi.utils.toast
import com.heyanle.lib_anim.utils.network.webview_helper.webViewHelper
import com.microsoft.appcenter.distribute.Distribute
import kotlinx.coroutines.launch
import org.jsoup.Jsoup


/**
 * Created by HeYanLe on 2023/1/7 22:53.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage() {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ThemeSettingCard()
        TinySettingCard()
        Author()
        About()
    }

}

@Composable
fun ThemeSettingCard(
    modifier: Modifier = Modifier
) {
    val themeState by EasyThemeController.easyThemeState
    val isDark = when (themeState.darkMode) {
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
                    }) {
                    if (it.name == themeState.themeMode.name) {
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
                    if (isDark) Icons.Filled.Brightness2 else Icons.Filled.WbSunny,
                    contentDescription = stringResource(id = R.string.dark_mode)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = stringResource(id = R.string.dark_mode))
            }


            Box() {
                val text = when (themeState.darkMode) {
                    DarkMode.Dark -> stringResource(id = R.string.dark_on)
                    DarkMode.Light -> stringResource(id = R.string.dark_off)
                    else -> stringResource(id = R.string.dark_auto)
                }
                Row() {
                    Text(
                        modifier = Modifier.alpha(0.6f),
                        text = text
                    )
                    Icon(
                        Icons.Filled.ExpandMore,
                        modifier = Modifier.alpha(0.6f),
                        contentDescription = stringResource(id = R.string.dark_mode)
                    )
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

        if (EasyThemeController.isSupportDynamicColor()) {
            var isDynamicCheck by remember {
                mutableStateOf(themeState.isDynamicColor)
            }
            LaunchedEffect(key1 = themeState) {
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
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ColorLens,
                        contentDescription = stringResource(id = R.string.is_dynamic_color)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Column() {
                        Text(text = stringResource(id = R.string.is_dynamic_color))
                        Text(
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            modifier = Modifier.alpha(0.6f),
                            text = stringResource(id = R.string.is_dynamic_color_msg)
                        )
                    }

                }
                Switch(checked = isDynamicCheck, onCheckedChange = {
                    isDynamicCheck = it
                    EasyThemeController.changeIsDynamicColor(it)
                    if (it) {
                        stringRes(R.string.dynamic_color_enable_msg).moeSnackBar()
                    }
                })
            }
        }

    }
}

@Composable
fun TinySettingCard(
    modifier: Modifier = Modifier
) {
    var isAutoTiny by remember {
        mutableStateOf(TinyStatusController.autoTinyEnableOkkv)
    }

    val ctx = LocalContext.current

    LaunchedEffect(key1 = isAutoTiny) {
        if (isAutoTiny && !OverlayHelper.drawOverlayEnable(ctx)) {
            isAutoTiny = false
            TinyStatusController.autoTinyEnableOkkv = false
        }
    }


    Column(
        modifier = Modifier
            .then(modifier),
    ) {

        Text(
            modifier = Modifier.padding(16.dp, 16.dp),
            text = stringResource(id = R.string.play),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Start
        )



        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val check = isAutoTiny
                if (!check) {
                    if (OverlayHelper.drawOverlayEnable(ctx)) {
                        isAutoTiny = true
                        TinyStatusController.autoTinyEnableOkkv = true
                    } else {
                        stringRes(R.string.please_overlay_permission).toast()
                        OverlayHelper.gotoDrawOverlaySetting(ctx)
                    }
                } else {
                    isAutoTiny = false
                    TinyStatusController.autoTinyEnableOkkv = false
                }
            }
            .padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LaptopWindows,
                    contentDescription = stringResource(id = R.string.auto_tiny)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Column() {
                    Text(text = stringResource(id = R.string.auto_tiny))
                    Text(
                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                        modifier = Modifier.alpha(0.6f),
                        text = stringResource(id = R.string.auto_tiny_msg)
                    )
                }

            }

            Switch(checked = isAutoTiny, onCheckedChange = {
                if (it) {
                    if (OverlayHelper.drawOverlayEnable(ctx)) {
                        isAutoTiny = true
                        TinyStatusController.autoTinyEnableOkkv = true
                    } else {
                        stringRes(R.string.please_overlay_permission).toast()
                        OverlayHelper.gotoDrawOverlaySetting(ctx)
                    }
                } else {
                    isAutoTiny = false
                    TinyStatusController.autoTinyEnableOkkv = false
                }
            })
        }


    }
}

@Composable
fun About(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .then(modifier),
    ) {

        Text(
            modifier = Modifier.padding(16.dp, 16.dp),
            text = stringResource(id = R.string.about),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Start
        )



        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Distribute.checkForUpdate()
            }
            .padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = stringResource(id = R.string.version)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = stringResource(id = R.string.version))
            }
            Text(text = BuildConfig.VERSION_NAME)

        }
        val manager: ClipboardManager = LocalClipboardManager.current
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                manager.setText(AnnotatedString("729848189"))
                "复制成功".moeSnackBar()
            }
            .padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Group,
                    contentDescription = stringResource(id = R.string.qq_groud)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = stringResource(id = R.string.qq_groud))
            }
            Text(text = "729848189")
        }


    }
}

@Composable
fun Author(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .then(modifier),
    ) {

        Text(
            modifier = Modifier.padding(16.dp, 16.dp),
            text = stringResource(id = R.string.author),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Start
        )



        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {

            }
            .padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OkImage(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    image = R.mipmap.app_logo,
                    contentDescription = "何言"
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = "何言")
            }
            Text(text = "整体研发")
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {

            }
            .padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OkImage(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    image = R.drawable.ayala,
                    contentDescription = "Ayala"
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = "ayala")
            }
            Text(text = "源适配")
        }


    }
}

