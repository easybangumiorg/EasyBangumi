package com.heyanle.easybangumi4.anime4k

import android.opengl.GLES30
import android.util.Log
import androidx.media3.common.util.UnstableApi

/**
 * 单个 Anime4K pass 的 GL 程序。
 *
 * fragment shader = mpv hook prelude + 原始 pass body + main() 包装：
 *  - 每个绑定纹理声明 `uniform sampler2D X; uniform vec2 X_size; uniform vec2 X_pt;`
 *    （uniform 名与 mpv 宏标识一致，body 中直接引用）；
 *  - `#define X_pos v_uv` —— 顶点插值的纹理坐标（mpv: texcoordN，本实现所有绑定
 *    纹理共享同一 quad 插值，0..1 覆盖输出面）；
 *  - `#define X_tex(c) texture(X, c)`、`#define X_texOff(x_off, y_off)
 *    X_tex(v_uv + vec2(x_off, y_off) * X_pt)`（mpv video.c hook_prelude 语义）；
 *  - HOOKED 别名为 hook 目标（mpv pass_hook_setup_binds 的 HOOKED 特殊名）。
 *
 * 采样约定与 Media3 自带效果一致：v_uv = aPosition * 0.5 + 0.5，
 * NDC(-1,-1) ↔ uv(0,0)，保证画面方向正确。
 */
@OptIn(UnstableApi::class)
internal class Anime4KProgram(
    private val pass: A4KPass,
    private val vertexShaderId: Int,
) {

    companion object {
        private const val TAG = "Anime4K"

        /** 共享顶点着色器：位置 → v_uv 线性映射（与 Media3 DefaultShaderProgram 约定一致）。
         * 注意必须 trimIndent：#version 必须在第一行（Adreno 强制），
         * 三重引号字符串的缩进/前导空行会导致编译失败。 */
        val VERTEX_SRC = """
            #version 300 es
            layout(location = 0) in vec2 aPosition;
            out vec2 v_uv;
            void main() {
                v_uv = aPosition * 0.5 + 0.5;
                gl_Position = vec4(aPosition, 0.0, 1.0);
            }
        """.trimIndent()
    }

    /** 绑定名（含 hook 目标；HOOKED 显式绑定保留原名，运行时解析为 hook 目标纹理） */
    val bindNames: List<String> = buildList {
        add(pass.hookTarget)
        for (b in pass.binds) {
            if (b != pass.hookTarget && !contains(b)) add(b)
        }
    }

    private var program = 0
    private val samplerLoc = HashMap<String, Int>()
    private val sizeLoc = HashMap<String, Int>()
    private val ptLoc = HashMap<String, Int>()

    fun compile(): Boolean {
        if (program != 0) return true
        val fsId = compileShader(GLES30.GL_FRAGMENT_SHADER, buildFragmentSource())
            ?: return false
        val p = GLES30.glCreateProgram()
        GLES30.glAttachShader(p, vertexShaderId)
        GLES30.glAttachShader(p, fsId)
        GLES30.glLinkProgram(p)
        GLES30.glDeleteShader(fsId)
        val status = IntArray(1)
        GLES30.glGetProgramiv(p, GLES30.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            val info = GLES30.glGetProgramInfoLog(p)
            Log.e(TAG, "Anime4K program link failed: $info (pass=${pass.desc})")
            GLES30.glDeleteProgram(p)
            return false
        }
        program = p
        for (name in bindNames) {
            samplerLoc[name] = GLES30.glGetUniformLocation(p, name)
            sizeLoc[name] = GLES30.glGetUniformLocation(p, "${name}_size")
            ptLoc[name] = GLES30.glGetUniformLocation(p, "${name}_pt")
        }
        return true
    }

    fun use() {
        GLES30.glUseProgram(program)
    }

    /** 绑定纹理 + 设置尺寸/texel 尺寸 uniform。w/h 为该纹理当前尺寸（像素）。 */
    fun bindTexture(name: String, texId: Int, w: Int, h: Int, unit: Int) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + unit)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId)
        samplerLoc[name]?.let { if (it >= 0) GLES30.glUniform1i(it, unit) }
        sizeLoc[name]?.let { if (it >= 0) GLES30.glUniform2f(it, w.toFloat(), h.toFloat()) }
        ptLoc[name]?.let { if (it >= 0) GLES30.glUniform2f(it, 1f / w, 1f / h) }
    }

    fun release() {
        if (program != 0) {
            GLES30.glDeleteProgram(program)
            program = 0
        }
    }

    private fun buildFragmentSource(): String {
        val sb = StringBuilder(4096)
        sb.append("#version 300 es\n")
        sb.append("precision highp float;\n")
        sb.append("precision highp int;\n")
        sb.append("in vec2 v_uv;\n")
        sb.append("out vec4 out_color;\n")
        for (name in bindNames) {
            sb.append("uniform sampler2D $name;\n")
            sb.append("uniform vec2 ${name}_size;\n")
            sb.append("uniform vec2 ${name}_pt;\n")
        }
        for (name in bindNames) {
            sb.append("#define ${name}_pos v_uv\n")
            sb.append("#define ${name}_tex(c) texture($name, c)\n")
            // mpv 语义：单参数 vec2 偏移（video.c hook_prelude 原文）
            sb.append("#define ${name}_texOff(off) ${name}_tex(v_uv + vec2(off) * ${name}_pt)\n")
        }
        if ("HOOKED" !in bindNames) {
            val t = pass.hookTarget
            sb.append("#define HOOKED_tex ${t}_tex\n")
            sb.append("#define HOOKED_texOff ${t}_texOff\n")
            sb.append("#define HOOKED_pos ${t}_pos\n")
            sb.append("#define HOOKED_pt ${t}_pt\n")
            sb.append("#define HOOKED_size ${t}_size\n")
        }
        var body = pass.body
        // Adreno GLSL ES 编译器：vec2(<非常量整数表达式>, ...) 构造产生垃圾坐标（运行时 0），
        // 且拒绝 float 与 const int 的混合运算/比较。
        // 变换顺序必须：先做 vec2 整数字面量转换（此时参数里还没有 float() 括号，
        // 正则能完整匹配），最后再套 float()（KERNELHALFSIZE / 循环条件）。
        body = Regex("for \\(int (\\w+)=0;").replace(body) { "for (float ${it.groupValues[1]}=0.0;" }
        body = Regex("vec2\\(([^,)]*), 0\\)").replace(body) { "vec2(${it.groupValues[1]}, 0.0)" }
        body = Regex("vec2\\(0, ([^,)]*)\\)").replace(body) { "vec2(0.0, ${it.groupValues[1]})" }
        body = Regex("(\\w+)<([A-Z][A-Z_0-9]*)").replace(body) { "${it.groupValues[1]}<float(${it.groupValues[2]})" }
        body = Regex("(\\w+) - KERNELHALFSIZE").replace(body) { "${it.groupValues[1]} - float(KERNELHALFSIZE)" }
        sb.append(body)
        if (!body.endsWith("\n")) sb.append("\n")
        sb.append("void main() { out_color = hook(); }\n")
        return sb.toString()
    }

    private fun compileShader(type: Int, source: String): Int? {
        val id = GLES30.glCreateShader(type)
        GLES30.glShaderSource(id, source)
        GLES30.glCompileShader(id)
        val status = IntArray(1)
        GLES30.glGetShaderiv(id, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val info = GLES30.glGetShaderInfoLog(id)
            Log.e(TAG, "Anime4K shader compile failed: $info (pass=${pass.desc})")
            GLES30.glDeleteShader(id)
            return null
        }
        return id
    }
}
