package com.heyanle.easybangumi4.compose.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.component.detailed.DetailedComponent
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.ABOUT
import com.heyanle.easybangumi4.C
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.source.SourceBundle
import com.heyanle.easybangumi4.source.SourceLibraryController
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toast
import com.heyanle.injekt.core.Injekt

/**
 * Created by HeYanLe on 2023/2/22 23:53.
 * https://github.com/heyanLE
 */
@Composable
fun SourceContainer(
    modifier: Modifier = Modifier,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (SourceBundle) -> Unit,
) {
    val sourceLibraryController: SourceLibraryController by Injekt.injectLazy()
    val animSources = LocalSourceBundleController.current
    val isLoading by sourceLibraryController.isLoading.collectAsState()
    val anim = animSources
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        if (isLoading) {
            LoadingPage(
                modifier = Modifier
                    .fillMaxSize(),
                loadingMsg = stringResource(id = R.string.source_loading)
            )
        } else if (anim.empty()) {
            val nav = LocalNavController.current
            ErrorPage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(errorContainerColor),
                errorMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_source),
                clickEnable = false,
                other = {
                    Column {
                        TextButton(onClick = {
                            stringRes(R.string.try_qq_group).toast()
                            kotlin.runCatching {
                                C.extensionUrl.openUrl()
                            }.onFailure {
                                it.printStackTrace()
                            }
                        }) {
                            Text(text = stringResource(id = R.string.website_get))
                        }

                        TextButton(onClick = {
                            stringRes(R.string.add_group_get).moeSnackBar()
                            nav.navigate(ABOUT)

                        }) {
                            Text(text = stringResource(id = R.string.group_get))
                        }
                    }


                }
            )
        } else {
            content(anim)
        }
    }
}

@Composable
fun SourceContainerBase(
    modifier: Modifier = Modifier,
    hasSource: (SourceBundle) -> Boolean,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (SourceBundle) -> Unit,
) {
    val animSources = LocalSourceBundleController.current
    val anim = animSources

    val sourceLibraryController: SourceLibraryController by Injekt.injectLazy()
    val isLoading by sourceLibraryController.isLoading.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        if (isLoading) {
            LoadingPage(
                modifier = Modifier
                    .fillMaxSize(),
                loadingMsg = stringResource(id = R.string.source_loading)
            )
        } else if (!hasSource(anim)) {
            val nav = LocalNavController.current
            ErrorPage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(errorContainerColor),
                errorMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_source),
                clickEnable = false,
                other = {
                    Column {
                        TextButton(onClick = {
                            stringRes(R.string.try_qq_group).toast()
                            kotlin.runCatching {
                                C.extensionUrl.openUrl()
                            }.onFailure {
                                it.printStackTrace()
                            }
                        }) {
                            Text(text = stringResource(id = R.string.website_get))
                        }

                        TextButton(onClick = {
                            stringRes(R.string.add_group_get).moeSnackBar()
                            nav.navigate(ABOUT)

                        }) {
                            Text(text = stringResource(id = R.string.group_get))
                        }
                    }


                }
            )
        } else {
            content(anim)
        }
    }
}

@Composable
fun PageContainer(
    sourceKey: String,
    modifier: Modifier = Modifier,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (SourceBundle, Source, List<SourcePage>) -> Unit,
) {
    SourceContainer(
        modifier,
        errorContainerColor = errorContainerColor
    ) {
        val homes = it.page(sourceKey)?.getPages() ?: emptyList()
        val sou = it.source(sourceKey)
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (sou == null) {
                val nav = LocalNavController.current
                ErrorPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(errorContainerColor),
                    errorMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_source),
                    clickEnable = false,
                    other = {
                        Column {
                            TextButton(onClick = {
                                stringRes(R.string.try_qq_group).toast()
                                kotlin.runCatching {
                                    C.extensionUrl.openUrl()
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            }) {
                                Text(text = stringResource(id = R.string.website_get))
                            }

                            TextButton(onClick = {
                                stringRes(R.string.add_group_get).moeSnackBar()
                                nav.navigate(ABOUT)

                            }) {
                                Text(text = stringResource(id = R.string.group_get))
                            }
                        }


                    }
                )
            } else {
                content(it, sou, homes)
            }
        }
    }
}

@Composable
fun DetailedContainer(
    sourceKey: String,
    modifier: Modifier = Modifier,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (SourceBundle, Source, DetailedComponent) -> Unit,
) {
    SourceContainer(
        modifier,
        errorContainerColor = errorContainerColor
    ) {
        val detailed = it.detailed(sourceKey)
        val sou = it.source(sourceKey)
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (sou == null || detailed == null) {
                val nav = LocalNavController.current
                ErrorPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(errorContainerColor),
                    errorMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_source),
                    clickEnable = false,
                    other = {
                        Column {
                            TextButton(onClick = {
                                stringRes(R.string.try_qq_group).toast()
                                kotlin.runCatching {
                                    C.extensionUrl.openUrl()
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            }) {
                                Text(text = stringResource(id = R.string.website_get))
                            }

                            TextButton(onClick = {
                                stringRes(R.string.add_group_get).moeSnackBar()
                                nav.navigate(ABOUT)

                            }) {
                                Text(text = stringResource(id = R.string.group_get))
                            }
                        }


                    }
                )
            } else {
                content(it, sou, detailed)
            }
        }
    }
}