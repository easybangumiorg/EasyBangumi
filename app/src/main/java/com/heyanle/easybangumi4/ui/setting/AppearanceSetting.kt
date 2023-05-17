package com.heyanle.easybangumi4.ui.setting

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.preferences.PadModePreferences
import com.heyanle.easybangumi4.theme.DarkMode
import com.heyanle.easybangumi4.theme.EasyThemeController
import com.heyanle.easybangumi4.theme.EasyThemeMode
import com.heyanle.easybangumi4.ui.common.IntPreferenceItem
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/22 17:04.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSetting() {

    val nav = LocalNavController.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.appearance_setting)) },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            stringResource(id = R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior

            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = stringResource(id = R.string.dark_mode),
                    color = MaterialTheme.colorScheme.primary
                )


                DarkModeItem()

                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = stringResource(id = R.string.theme),
                    color = MaterialTheme.colorScheme.primary
                )

                ThemeModeItem()

                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = stringResource(id = R.string.show),
                    color = MaterialTheme.colorScheme.primary
                )

                IntPreferenceItem(
                    title = { Text(text = stringResource(id = R.string.pad_mode)) },
                    textList = remember {
                        listOf(
                            stringRes(R.string.auto),
                            stringRes(R.string.always_on),
                            stringRes(R.string.always_off),
                        )
                    },
                    preference = PadModePreferences
                ){
                    scope.launch {
                        stringRes(R.string.some_page_should_reboot).moeSnackBar()
                    }
                }

            }


        }
    }


}

@Composable
fun DarkModeItem() {
    val theme = EasyThemeController.easyThemeState.value
    val list = listOf(
        Triple(Icons.Filled.Android, stringRes(R.string.dark_auto), DarkMode.Auto),
        Triple(Icons.Filled.WbSunny, stringRes(R.string.dark_off), DarkMode.Light),
        Triple(Icons.Filled.NightsStay, stringRes(R.string.dark_on), DarkMode.Dark)
    )

    val enableColor = MaterialTheme.colorScheme.primary
    val disableColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
    ) {
        list.forEachIndexed { index, (image, text, mode) ->
            val currentColor = if (theme.darkMode == mode) enableColor else disableColor
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .border(
                        width = 1.dp,
                        color = currentColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        if (theme.darkMode != mode) {
                            EasyThemeController.changeDarkMode(mode)
                        }
                    }
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = image,
                    contentDescription = text,
                    tint = currentColor,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(text = text, color = currentColor)
            }
        }
    }


}

//val lazyListState = LazyListState()

@Composable
fun ThemeModeItem() {
    val theme = EasyThemeController.easyThemeState.value
    val isDark = when (theme.darkMode) {
        DarkMode.Dark -> true
        DarkMode.Light -> false
        DarkMode.Auto -> isSystemInDarkTheme()
    }

    val context = LocalContext.current


    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(start = 6.dp, end = 6.dp)
    ) {
        if (EasyThemeController.isSupportDynamicColor() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val dynamicColor =
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            item {
                ThemePreviewItem(
                    selected = theme.isDynamicColor,
                    colorScheme = dynamicColor,
                    stringResource(id = R.string.is_dynamic_color)
                ) {
                    if (!theme.isDynamicColor) {
                        EasyThemeController.changeIsDynamicColor(true)
                    }

                }
            }
        }
        items(EasyThemeMode.values()) {
            ThemePreviewItem(
                selected = !theme.isDynamicColor && theme.themeMode == it,
                colorScheme = if (isDark) it.darkColorScheme else it.lightColorScheme,
                stringResource(id = it.titleResId)
            ) {
                EasyThemeController.changeThemeMode(it, false)
            }

        }
    }
}

@Composable
fun ThemePreviewItem(
    selected: Boolean,
    colorScheme: ColorScheme,
    title: String,
    onClick: () -> Unit,
) {
    val dividerColor = colorScheme.onSurface.copy(alpha = 0.2f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(9f / 16f)
                .border(
                    width = 4.dp,
                    color = if (selected) {
                        colorScheme.primary
                    } else {
                        dividerColor
                    },
                    shape = RoundedCornerShape(17.dp),
                )
                .padding(4.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(colorScheme.background)
                .clickable(onClick = onClick),
        ) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .weight(0.7f)
                        .padding(end = 4.dp)
                        .background(
                            color = colorScheme.onSurface,
                            shape = MaterialTheme.shapes.small,
                        ),
                )

                Box(
                    modifier = Modifier.weight(0.3f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = stringResource(R.string.theme),
                            tint = colorScheme.primary,
                        )
                    }
                }
            }

            // Cover
            Box(
                modifier = Modifier
                    .padding(start = 8.dp, top = 2.dp)
                    .background(
                        color = dividerColor,
                        shape = MaterialTheme.shapes.small,
                    )
                    .fillMaxWidth(0.5f)
                    .aspectRatio(19 / 27F),
            ) {
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(width = 24.dp, height = 16.dp)
                        .clip(RoundedCornerShape(5.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(12.dp)
                            .background(colorScheme.tertiary),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(12.dp)
                            .background(colorScheme.secondary),
                    )
                }
            }

            // Bottom bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    tonalElevation = 3.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .fillMaxWidth()
                            .background(colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(17.dp)
                                .background(
                                    color = colorScheme.primary,
                                    shape = CircleShape,
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .alpha(0.6f)
                                .height(17.dp)
                                .weight(1f)
                                .background(
                                    color = colorScheme.onSurface,
                                    shape = MaterialTheme.shapes.small,
                                ),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
    }

}