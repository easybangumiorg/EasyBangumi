package org.easybangumi.next.shared.compose.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
import org.easybangumi.next.shared.compose.common.EnumPreferenceItem
import org.easybangumi.next.shared.compose.common.StringPreferenceItem
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.ktor.parseManualProxyEndpoint
import org.easybangumi.next.shared.preference.NetworkPreference
import org.easybangumi.next.shared.resources.Res
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun SettingPage(onBack: () -> Unit) {
    val networkPreference = koinInject<NetworkPreference>()
    var currentPage by remember { mutableStateOf<String?>(null) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentPage) {
                            "network" -> "Network settings"
                            else -> stringRes(Res.strings.setting)
                        }
                    )
                },
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
                "network" -> NetworkSettingContent(networkPreference, scrollBehavior.nestedScrollConnection)
                else -> FirstSettingContent(
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    onNavigate = { currentPage = it },
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ListItem(
            modifier = Modifier.clickable { onNavigate("network") },
            headlineContent = { Text(text = "Network settings") },
            leadingContent = {
                Icon(Icons.Filled.Settings, contentDescription = "Network settings")
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun ColumnScope.NetworkSettingContent(
    networkPreference: NetworkPreference,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
) {
    val proxyMode by networkPreference.proxyMode.flow().collectAsState(networkPreference.proxyMode.get())

    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EnumPreferenceItem(
            title = { Text("Proxy mode") },
            textList = listOf("Disabled", "Manual", "Follow system"),
            preference = networkPreference.proxyMode,
        )

        if (proxyMode == NetworkPreference.ProxyMode.MANUAL) {
            EnumPreferenceItem(
                title = { Text("Proxy protocol") },
                textList = listOf("HTTP", "SOCKS5"),
                preference = networkPreference.proxyProtocol,
            )

            StringPreferenceItem(
                title = { Text("Proxy URL") },
                preference = networkPreference.proxyUrl,
                placeholder = "127.0.0.1:7890",
                supportingText = "Use host:port or URL. Proxy settings take effect after restarting the app.",
                isError = { parseManualProxyEndpoint(it) == null },
            )
        } else {
            Text(
                text = "Proxy settings take effect after restarting the app.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
