package com.heyanle.easybangumi4.anime4k

/**
 * mpv user-shader RPN 表达式求值（//!WIDTH / //!HEIGHT / //!WHEN 共用）。
 *
 * 语义逐行对照 mpv video/out/gpu/user_shaders.c 的 parse_rpn_szexpr / eval_szexpr：
 *  - 变量形如 `X.w` / `X.h`（也接受 .width / .height 后缀），由 sizeOf 回调解析；
 *  - 运算符 `+ - * / % ! > < =`，RPN（后缀）顺序，pop 顺序为 op1 = 先入栈者；
 *  - 常量按浮点解析；未知标识符由 constOf 回调解析（本 shader 集无 //!PARAM，恒 null）。
 *  - 求值失败返回 null（调用方自行决定跳过/报错）。
 */
internal object A4KRpn {

    fun eval(
        expr: String,
        sizeOf: (String) -> Pair<Float, Float>?,
        constOf: (String) -> Float?,
    ): Float? {
        val stack = ArrayList<Float>(16)
        val tokens = expr.trim().split(Regex("\\s+"))
        for (tok in tokens) {
            if (tok.isEmpty()) continue
            when {
                tok.endsWith(".w") || tok.endsWith(".width") -> {
                    val name = tok.substring(0, tok.lastIndexOf('.'))
                    val s = sizeOf(name) ?: return null
                    stack.add(s.first)
                }

                tok.endsWith(".h") || tok.endsWith(".height") -> {
                    val name = tok.substring(0, tok.lastIndexOf('.'))
                    val s = sizeOf(name) ?: return null
                    stack.add(s.second)
                }

                tok == "+" -> binop(stack) { a, b -> a + b }
                tok == "-" -> binop(stack) { a, b -> a - b }
                tok == "*" -> binop(stack) { a, b -> a * b }
                tok == "/" -> binop(stack) { a, b -> a / b }
                tok == "%" -> binop(stack) { a, b -> a % b }
                tok == "!" -> unop(stack) { a -> if (a == 0f) 1f else 0f }
                tok == ">" -> binop(stack) { a, b -> if (a > b) 1f else 0f }
                tok == "<" -> binop(stack) { a, b -> if (a < b) 1f else 0f }
                tok == "=" -> binop(stack) { a, b -> if (a == b) 1f else 0f }

                else -> {
                    val c = tok.toFloatOrNull()
                        ?: constOf(tok)
                        ?: return null
                    stack.add(c)
                }
            }
        }
        return stack.lastOrNull()
    }

    private fun binop(stack: MutableList<Float>, f: (Float, Float) -> Float): Boolean {
        if (stack.size < 2) return false
        val b = stack.removeAt(stack.size - 1)
        val a = stack.removeAt(stack.size - 1)
        stack.add(f(a, b))
        return true
    }

    private fun unop(stack: MutableList<Float>, f: (Float) -> Float): Boolean {
        if (stack.isEmpty()) return false
        val a = stack.removeAt(stack.size - 1)
        stack.add(f(a))
        return true
    }
}
