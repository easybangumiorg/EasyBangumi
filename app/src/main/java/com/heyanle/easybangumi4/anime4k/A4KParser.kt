package com.heyanle.easybangumi4.anime4k

/**
 * Anime4K v4（mpv hook 格式）pass 解析器。
 *
 * 语义对照 mpv video/out/gpu/user_shaders.c parse_user_shader / parse_hook：
 *  - 文件先跳过 `//!` 之前的所有内容（许可证注释等）；
 *  - 每个 pass = 连续的 `//!` 魔法行块 + 到下一个 `//!` 为止的 GLSL body；
 *  - `//!HOOK X` 声明挂钩纹理（本 shader 集为 MAIN 或 PREKERNEL）；
 *  - `//!BIND X` 声明额外绑定纹理（HOOKED = 当前挂钩纹理的别名）；
 *  - `//!SAVE X` 声明输出纹理名；缺省时输出覆盖挂钩纹理（mpv: store_name = save ?: hook 名）；
 *  - `//!WIDTH / //!HEIGHT` 为 RPN 尺寸表达式（缺省 = 挂钩纹理尺寸）；
 *  - `//!WHEN` 为 RPN 布尔条件（缺省恒真）。
 */
data class A4KPass(
    val desc: String = "",
    val hooks: List<String> = emptyList(),
    val binds: List<String> = emptyList(),
    val save: String = "",
    val widthExpr: String = "",
    val heightExpr: String = "",
    val components: Int = 0,
    val whenExpr: String? = null,
    val body: String = "",
) {
    /** mpv 语义：无 //!SAVE 时输出覆盖挂钩纹理 */
    val effectiveSave: String get() = if (save.isNotEmpty()) save else (hooks.firstOrNull() ?: "MAIN")

    val hookTarget: String get() = hooks.firstOrNull() ?: "MAIN"
}

internal object A4KParser {

    fun parse(source: String): List<A4KPass> {
        val passes = ArrayList<A4KPass>()
        var i = source.indexOf("//!")
        if (i < 0) return passes
        while (i >= 0) {
            // 收集连续魔法行块
            var j = i
            val header = StringBuilder()
            while (j < source.length) {
                val lineEnd = source.indexOf('\n', j)
                val line = source.substring(j, if (lineEnd < 0) source.length else lineEnd).trim()
                if (!line.startsWith("//!")) break
                header.append(line).append('\n')
                j = if (lineEnd < 0) source.length else lineEnd + 1
            }
            // body = 到下一个 //! 为止
            val nextMagic = source.indexOf("//!", j)
            val body = if (nextMagic < 0) source.substring(j) else source.substring(j, nextMagic)

            val pass = parseHeader(header.toString())
            if (pass != null && pass.hooks.isNotEmpty()) {
                passes.add(pass.copy(body = body))
            }
            i = nextMagic
        }
        return passes
    }

    private fun parseHeader(header: String): A4KPass? {
        var p = A4KPass()
        val hooks = ArrayList<String>()
        val binds = ArrayList<String>()
        var save = ""
        var widthExpr = ""
        var heightExpr = ""
        var components = 0
        var whenExpr: String? = null
        var desc = ""

        for (line in header.lineSequence()) {
            val rest = line.trim().removePrefix("//!").trim()
            val sp = rest.indexOf(' ')
            val cmd = if (sp < 0) rest else rest.substring(0, sp)
            val arg = if (sp < 0) "" else rest.substring(sp + 1).trim()
            when (cmd) {
                "DESC" -> desc = arg
                "HOOK" -> hooks.add(arg)
                "BIND" -> binds.add(arg)
                "SAVE" -> save = arg
                "WIDTH" -> widthExpr = arg
                "HEIGHT" -> heightExpr = arg
                "COMPONENTS" -> components = arg.toIntOrNull() ?: 0
                "WHEN" -> whenExpr = arg
                // ALIGN/OFFSET/COMPUTE/PARAM/TEXTURE 等：本 shader 集未使用，忽略
            }
        }
        p = p.copy(
            desc = desc, hooks = hooks, binds = binds, save = save,
            widthExpr = widthExpr, heightExpr = heightExpr,
            components = components, whenExpr = whenExpr,
        )
        return p
    }
}
