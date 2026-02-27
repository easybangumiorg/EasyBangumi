package org.easybangumi.next.shared.compose.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.common.BooleanPreferenceItem
import org.easybangumi.next.shared.compose.common.EnumPreferenceItem
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.preference.AndroidPlayerPreference
import org.easybangumi.next.shared.preference.MainPreference
import org.easybangumi.next.shared.resources.Res
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun SettingPage(onBack: () -> Unit) {
    val mainPreference = koinInject<MainPreference>()
    val playerPreference = koinInject<AndroidPlayerPreference>()

    var currentPage by remember { mutableStateOf<String?>(null) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val title = when (currentPage) {
        "appearance" -> stringRes(Res.strings.appearance_setting)
        "player" -> stringRes(Res.strings.player_setting)
        else -> stringRes(Res.strings.setting)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentPage != null) {
                            currentPage = null
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringRes(Res.strings.back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )

            when (currentPage) {
                "appearance" -> AppearanceSettingContent(mainPreference, scrollBehavior.nestedScrollConnection)
                "player" -> PlayerSettingContent(playerPreference, scrollBehavior.nestedScrollConnection)
                else -> FirstSettingContent(
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    onNavigate = { currentPage = it }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.FirstSettingContent(
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    onNavigate: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ListItem(
            modifier = Modifier.clickable { onNavigate("appearance") },
            headlineContent = { Text(text = stringRes(Res.strings.appearance_setting)) },
            leadingContent = {
                Icon(Icons.Filled.ColorLens, contentDescription = stringRes(Res.strings.appearance_setting))
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        ListItem(
            modifier = Modifier.clickable { onNavigate("player") },
            headlineContent = { Text(text = stringRes(Res.strings.player_setting)) },
            leadingContent = {
                Icon(Icons.Filled.PlayCircle, contentDescription = stringRes(Res.strings.player_setting))
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun ColumnScope.AppearanceSettingContent(
    mainPreference: MainPreference,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier,
            text = stringRes(Res.strings.pad_mode),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
        )
        EnumPreferenceItem(
            title = { Text(text = stringRes(Res.strings.pad_mode)) },
            textList = listOf(
                stringRes(Res.strings.auto),
                stringRes(Res.strings.always_on),
                stringRes(Res.strings.always_off),
            ),
            preference = mainPreference.tabletMode,
        )
    }
}

@Composable
private fun ColumnScope.PlayerSettingContent(
    playerPreference: AndroidPlayerPreference,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EnumPreferenceItem(
            title = { Text(stringRes(Res.strings.player_orientation_mode)) },
            textList = listOf(
                stringRes(Res.strings.auto),
                stringRes(Res.strings.always_on),
                stringRes(Res.strings.always_off),
            ),
            preference = playerPreference.autoFullScreenMode,
        )
    }
}
