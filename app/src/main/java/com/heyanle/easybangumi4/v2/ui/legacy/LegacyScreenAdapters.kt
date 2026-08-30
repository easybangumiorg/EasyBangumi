package com.heyanle.easybangumi4.v2.ui.legacy

import android.webkit.WebView
import androidx.compose.runtime.Composable
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonPlay
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.common.SourceContainer
import com.heyanle.easybangumi4.v2.ui.main.MainV2Shell
import com.heyanle.easybangumi4.v2.ui.home.HomeV2
import com.heyanle.easybangumi4.v2.ui.history.HistoryV2
import com.heyanle.easybangumi4.v2.ui.more.MoreV2
import com.heyanle.easybangumi4.v2.ui.star.StarV2
import com.heyanle.easybangumi4.v2.ui.setting.SettingV2
import com.heyanle.easybangumi4.v2.ui.about.AboutV2
import com.heyanle.easybangumi4.v2.ui.tags.CartoonTagV2
import com.heyanle.easybangumi4.v2.ui.storage.StorageV2
import com.heyanle.easybangumi4.v2.ui.story.StoryV2
import com.heyanle.easybangumi4.v2.ui.source.SourceManagerV2
import com.heyanle.easybangumi4.v2.ui.source.SourcePushV2
import com.heyanle.easybangumi4.v2.ui.source.SourceConfigV2
import com.heyanle.easybangumi4.v2.ui.search.SearchV2
import com.heyanle.easybangumi4.v2.ui.migrate.MigrateV2
import com.heyanle.easybangumi4.v2.ui.web.WebVerificationV2
import com.heyanle.easybangumi4.v2.ui.dlna.DlnaV2

/**
 * Temporary V2-to-legacy screen boundary.
 *
 * Every registered V2 destination enters the existing UI through one explicitly named adapter.
 * This keeps the V2 graph complete while allowing screens to be replaced one at a time without
 * copying ViewModels or business state into the new package.
 */
@Composable
internal fun LegacyMainScreen() {
    MainV2Shell()
}

@Composable
internal fun LegacyHomeScreen() {
    // HomeV2 owns presentation locally; V1 Home remains untouched for rollback.
    SourceContainer { HomeV2() }
}

@Composable
internal fun LegacyStarScreen() {
    SourceContainer { StarV2() }
}

@Composable
internal fun LegacyMoreScreen() {
    MoreV2()
}

@Composable
internal fun LegacyPlaybackDetailRollbackScreen(
    id: String,
    source: String,
    enterData: CartoonPlayViewModel.EnterData?,
) {
    CartoonPlay(id = id, source = source, enterData = enterData)
}

@Composable
internal fun LegacyDlnaScreen(
    id: String,
    source: String,
    enterData: CartoonPlayViewModel.EnterData?,
) {
    DlnaV2(id = id, source = source, enterData = enterData)
}

/** The legacy graph also registers this destination without rendering a local-player screen. */
@Composable
@Suppress("UNUSED_PARAMETER")
internal fun LegacyLocalPlayPlaceholderScreen(uuid: String) = Unit

@Composable
internal fun LegacySettingScreen(router: String) {
    SettingV2(router = router)
}

@Composable
internal fun LegacyWebVerificationScreen(
    webView: WebView,
    tips: String,
    onCheck: (WebView) -> Boolean,
    onStop: (WebView) -> Unit,
) {
    WebVerificationV2(webView = webView, tips = tips, onCheck = onCheck, onStop = onStop)
}

@Composable
internal fun LegacyHistoryScreen() {
    HistoryV2()
}

@Composable
internal fun LegacyStoryScreen(initialPage: Int) {
    StoryV2(initialPage = initialPage)
}

@Composable
internal fun LegacySourcePushScreen() {
    SourcePushV2()
}

@Composable
internal fun LegacySourceManagerScreen(initialPage: Int) {
    SourceManagerV2(initialPage = initialPage)
}

@Composable
internal fun LegacySearchScreen(
    initialKeyword: String,
    initialSourceKey: String,
) {
    SearchV2(initialKeyword = initialKeyword, initialSourceKey = initialSourceKey)
}

@Composable
internal fun LegacyCartoonMigrateScreen(
    summaries: List<CartoonSummary>,
    sourceKeys: List<String>,
) {
    MigrateV2(summaries = summaries, sourceKeys = sourceKeys)
}

@Composable
internal fun LegacyAboutScreen() {
    AboutV2()
}

@Composable
internal fun LegacySourceConfigScreen(sourceKey: String) {
    SourceConfigV2(sourceKey = sourceKey)
}

@Composable
internal fun LegacyTagManageScreen() {
    CartoonTagV2()
}

@Composable
internal fun LegacyStorageScreen() {
    StorageV2()
}
