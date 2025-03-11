package com.heyanle.easy_bangumi_cm.common.plugin.core.source

import com.heyanle.easy_bangumi_cm.common.plugin.core.component.ComponentClazz
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeComponent
import kotlin.reflect.KClass

/**
 * Created by heyanlin on 2024/12/9.
 */
class SourceBundle(
    loadedInfoList: List<SourceInfo.Loaded>,
) {

    // 已排序
    private val map = linkedMapOf<String, SourceInfo.Loaded>()

    // 查询存在某个 Component 的 SourceInfo.Loaded
    private val clazzInBundle: HashMap<KClass<*>, List<SourceInfo.Loaded>> = hashMapOf()


    init {
        // 排序
        val list = loadedInfoList.sortedBy { it.sourceConfig.order }
        for (loaded in list) {
            map[loaded.sourceConfig.key] = loaded
            for (componentClazz in ComponentClazz.componentClazz) {
                val component = loaded.componentBundle.getComponent(componentClazz)
                if (component != null) {
                    val bundleList = clazzInBundle[componentClazz] ?: listOf()
                    clazzInBundle[componentClazz] = bundleList + loaded
                }
            }
        }
    }


    fun keys() = map.keys

    // 查询 sourceInfo 数据
    fun sourceInfo(key: String) = map[key]
    fun config(key: String) = map[key]?.sourceConfig
    fun componentBundle(key: String) = map[key]?.componentBundle

    // 查询 Component 数据
    fun homeComponentInfoList() = clazzInBundle[HomeComponent::class] ?: emptyList()
    fun homeComponentInfo(key: String) = if (homeComponent(key) == null) null else map[key]
    fun homeComponent(key: String) = map[key]?.componentBundle?.getComponent(HomeComponent::class)




}