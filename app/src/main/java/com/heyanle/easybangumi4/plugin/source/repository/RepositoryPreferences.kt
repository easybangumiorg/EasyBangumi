package com.heyanle.easybangumi4.plugin.source.repository

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.base.json.JsonlFileHelper
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.getFilePath
import com.hippo.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import java.io.File
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

class RepositoryPreferences {
    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.newSingleDispatcher)

    val repositories: JsonlFileHelper<RepositoryInfo> = JsonlFileHelper(
        folder = UniFile.fromFile(File(APP.getFilePath("repository")))!!,
        name = "repository_list.jsonl",
        scope = scope,
        type = typeOf<RepositoryInfo>().javaType,
    )

    val activeRepositories = repositories.requestFlow
        .map { activeRepositories(it) }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getActiveRepositories(): List<RepositoryInfo> =
        activeRepositories(repositories.getOrDef())
}
