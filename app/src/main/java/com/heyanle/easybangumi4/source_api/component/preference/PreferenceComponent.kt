package com.heyanle.easybangumi4.source_api.component.preference


import com.heyanle.easybangumi4.source_api.component.Component

/**
 * register 方法中进行配置的注册
 * 然后全局里可以使用
 * Created by HeYanLe on 2023/10/18 23:31.
 * https://github.com/heyanLE
 */
interface PreferenceComponent: Component {

    /**
     * 注册配置，宿主会根据该列表为该源生成一个配置页面
     * 可以通过注入 PreferenceHelper 来获取用户选择的数据，通过 key 区分
     */
    fun register(): List<SourcePreference>

    /**
     * @param oldVersionCode 旧版的 source version code
     * @return 是否需要触发配置更新
     */
    fun needMigrate(oldVersionCode: Int): Boolean {
        return false
    }

    /**
     * 当 needMigrate 返回 true 时会调用该方法更新配置
     * @param oldMap 旧版数据的 map
     * @param oldVersionCode 旧版的 source version code
     * @return 迁移后的 map
     */
    fun onMigrate(oldMap: Map<String, String>, oldVersionCode: Int): Map<String, String> {
        return if (needMigrate(oldVersionCode)) {
            throw IllegalStateException("source preference need migrate but no onMigrate: (${this})")
        } else {
            oldMap
        }
    }

}