package com.heyanle.easybangumi4.source.utils

import android.content.Context
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.utils.getDataPath
import com.heyanle.lib_anim.utils.SourceContext
import com.heyanle.lib_anim.utils.preference.PreferenceHelper
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by HeYanLe on 2023/8/5 16:56.
 * https://github.com/heyanLE
 */
class SourcePreferenceHelper(
    context: Context,
    source: SourceContext,
) : PreferenceHelper {

    companion object {
        private val map: ConcurrentHashMap<SourceContext, SourcePreferenceHelper> =
            ConcurrentHashMap()

        fun of(context: Context, source: SourceContext): SourcePreferenceHelper {
            val helper = map[source] ?: SourcePreferenceHelper(context, source = source)
            map[source] = helper
            return helper
        }
    }


    private val hekv = HeKV(context.getDataPath("source-preference"), source.key)

    override fun load(key: String, def: String): String {
        return hekv.get(key, def)
    }

    override fun save(key: String, value: String) {
        hekv.put(key, value)
    }

    fun hekv(): HeKV {
        return hekv
    }
}