package com.heyanle.easybangumi4.source

import android.app.Application
import com.heyanle.bangumi_source_api.api.MigrateSource
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.component.configuration.ConfigComponent
import com.heyanle.bangumi_source_api.api.component.detailed.DetailedComponent
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.easybangumi4.base.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.base.entity.CartoonStar
import com.heyanle.easybangumi4.preferences.SourcePreferences
import com.heyanle.easybangumi4.source.utils.SourcePreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * 源更新配置
 * Created by HeYanLe on 2023/8/5 20:14.
 * https://github.com/heyanLE
 */
class SourceMigrationController(
    private val context: Application,
    private val sourcePreferences: SourcePreferences,
    private val cartoonStarDao: CartoonStarDao,
) {

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _migratingSource: MutableStateFlow<Set<Source>> = MutableStateFlow(emptySet())
    val migratingSource = _migratingSource.asStateFlow()

    private val _logs: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val logs = _logs.asStateFlow()


    fun migration(
        list: List<Source>,
    ) {
        list.forEach {
            scope.launch {
                innerMigration(it)
            }
        }

    }

    private suspend fun innerMigration(
        source: Source
    ) {
        if (_migratingSource.value.contains(source)) {
            return
        }
        // 源没支持迁移
        if (source !is MigrateSource) {
            return
        }
        // 源表示不用迁移
        val vp = sourcePreferences.getLastVersion(source)
        if (!source.needMigrate(vp.get())) {
            vp.set(source.versionCode)
            return
        }
        val detailed = source.components().filterIsInstance<DetailedComponent>().firstOrNull()
        val config = source.components().filterIsInstance<ConfigComponent>().firstOrNull()

        if (detailed == null && config == null) {
            return
        }

        // 开始迁移
        _migratingSource.update {
            it + source
        }

        log("${source.label}@${source.key} 开始数据升级 ${vp.get()} -> ${source.versionCode}")

        if (detailed != null) {
            log("收藏番剧数据升级开始")
            // 收藏迁移
            val stars = cartoonStarDao.getAllBySource(source.key)
            val summaries = stars.map {
                CartoonSummary(it.id, it.source, it.url)
            }

            val newSummaries = source.onMigrate(summaries, vp.get())
            log("summary 更新 ${summaries.size} -> ${newSummaries.size}")
            log("开始拉取番剧详细信息")
            val newStars = newSummaries.flatMap {
                val res = detailed.getAll(it)
                when (res) {
                    is SourceResult.Complete -> {
                        log("拉取成功 ${it.id} ${res.data.first.title}")
                        listOf(CartoonStar.fromCartoon(res.data.first, res.data.second))
                    }

                    else -> {
                        log("拉取失败 ${it.id}")
                        emptyList<CartoonStar>()
                    }
                }
            }
            log("更新数据库 ${stars.size} -> ${newStars.size}")
            cartoonStarDao.migration(stars, newStars)
        }
        if (config != null) {
            log("开始源配置迁移")
            // 源配置迁移
            val hekv = SourcePreferenceHelper.of(context, source).hekv()
            val oldMap = hekv.map()
            val newMap = config.onMigrate(oldMap, vp.get())
            oldMap.iterator().forEach {
                hekv.remove(it.key)
            }
            newMap.iterator().forEach {
                hekv.put(it.key, it.value)
            }
        }

        vp.set(source.versionCode)
        _migratingSource.update {
            it - source
        }
        log("${source.label}@${source.key} 数据升级完成 ${source.versionCode}")


    }

    fun clear(){
        _logs.update {
            emptyList()
        }
    }

    private fun log(log: String) {
        _logs.update {
            it + log
        }
    }

}