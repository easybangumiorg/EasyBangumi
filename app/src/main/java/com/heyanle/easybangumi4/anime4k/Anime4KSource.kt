package com.heyanle.easybangumi4.anime4k

import android.content.Context
import android.util.Log

/**
 * Anime4K shader 资产加载与链构建入口。
 *
 * 资产：app/src/main/assets/anime4k/ 下的 .glsl 文件（Anime4K v4 官方发布版）
 * （Anime4K v4 hook 格式，MIT License，Copyright (c) 2019-2021 bloc97）。
 */
object Anime4KSource {

    private const val TAG = "Anime4K"
    private const val ASSET_DIR = "anime4k"

    @Volatile
    private var parsed: Map<String, List<A4KPass>>? = null

    /** 解析全部 shader 资产（懒加载 + 缓存）。失败返回空 map。 */
    fun passes(context: Context): Map<String, List<A4KPass>> {
        parsed?.let { return it }
        synchronized(this) {
            parsed?.let { return it }
            val result = HashMap<String, List<A4KPass>>()
            try {
                val list = context.assets.list(ASSET_DIR) ?: emptyArray()
                for (name in list) {
                    if (!name.endsWith(".glsl")) continue
                    val source = context.assets.open("$ASSET_DIR/$name").bufferedReader().use { it.readText() }
                    val ps = A4KParser.parse(source)
                    result[name] = ps
                    Log.d(TAG, "loaded $name: ${ps.size} passes")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Anime4K assets load failed", e)
            }
            parsed = result
            return result
        }
    }

    /** 构建并按 mpv 阶段调度排序的链 pass 列表。 */
    fun chainFor(context: Context, mode: Int, quality: String): List<A4KPass> {
        val files = passes(context)
        val chain = A4KChain.build(mode, quality) { files[it] }
        return A4KChain.schedule(chain)
    }
}
