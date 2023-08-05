package com.heyanle.bangumi_source_api.api.component.configuration

import androidx.annotation.Keep
import com.heyanle.bangumi_source_api.api.component.Component

/**
 * Created by HeYanLe on 2023/8/4 22:56.
 * https://github.com/heyanLE
 */
@Keep
interface ConfigComponent : Component {


    fun configs(): List<SourceConfig>

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
            throw IllegalStateException("source config need migrate but no onMigrate: (${source})")
        } else {
            oldMap
        }
    }

}