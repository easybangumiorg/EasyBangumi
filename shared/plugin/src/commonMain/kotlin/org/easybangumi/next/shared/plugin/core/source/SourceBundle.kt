package org.easybangumi.next.shared.plugin.core.source

import org.easybangumi.next.shared.plugin.ConstClazz
import org.easybangumi.next.shared.plugin.info.SourceInfo
import kotlin.collections.set
import kotlin.reflect.KClass

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
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
            for (componentClazz in ConstClazz.componentClazz) {
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

//    // 查询 Component 数据
//    fun homeComponentInfoList() = clazzInBundle[HomeComponent::class] ?: emptyList()
//    fun homeComponentInfo(key: String) = if (homeComponent(key) == null) null else map[key]
//    fun homeComponent(key: String) = map[key]?.componentBundle?.getComponent(HomeComponent::class)
//
//
//

}