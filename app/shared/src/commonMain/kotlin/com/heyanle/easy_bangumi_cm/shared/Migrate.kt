package com.heyanle.easy_bangumi_cm.shared

import androidx.room.migration.Migration
import com.heyanle.easy_bangumi_cm.base.model.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.model.system.IPlatformInformation
import com.heyanle.lib.inject.api.get
import com.heyanle.lib.inject.core.Inject
import com.heyanle.lib.unifile.UniFileFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.URI

/**
 * Created by heyanlin on 2024/12/5.
 */
object Migrate {

    private val _isMigration = MutableStateFlow<Boolean>(true)
    val isMigration = _isMigration.asStateFlow()

    // Business Migrate
    fun migrate() {
        val platform = Inject.get<IPlatformInformation>()
        val pathProvider = Inject.get<IPathProvider>()
        val versionLog = pathProvider.getFilePath("migrate.version.txt")
        val versionFile = UniFileFactory.fromUri(URI.create("file://$versionLog"))
        val currentVersion = versionFile?.openInputStream()?.bufferedReader()?.use {
            it.readText().toIntOrNull()
        }?: -1
        val newVersion = platform.versionCode

        // 业务迁移开始

        // 业务迁移结束

        if (currentVersion >= newVersion) {
            versionFile?.delete()
            versionFile?.openOutputStream()?.bufferedWriter()?.use {
                it.write(newVersion.toString())
            }
            _isMigration.update { false }
            return
        }
    }


}