package com.heyanle.easy_bangumi_cm.unifile

import java.io.Closeable

/**
 * Created by heyanlin on 2024/12/4.
 */
fun Closeable.safeClose(){
    try {
        this.close()
    } catch (e: Exception){
        e.printStackTrace()
    }
}

fun Closeable.safeCloseNonRuntime(){
    try {
        this.close()
    } catch (e: RuntimeException) {
        throw e
    }catch (e: Exception){
        e.printStackTrace()
    }
}

object UniUtils  {
    fun resolve(parent: String, child: String): String {
        if (child.length == 0 || child == "/") {
            return parent
        }

        if (child[0] == '/') {
            if (parent == "/") return child
            return parent + child
        }

        if (parent == "/") return parent + child
        return "$parent/$child"
    }

    /**
     * A normal Unix pathname does not contain consecutive slashes and does not end
     * with a slash. The empty string and "/" are special cases that are also
     * considered normal.
     */
    fun normalize(pathname: String): String {
        val n = pathname.length
        val normalized = pathname.toCharArray()
        var index = 0
        var prevChar = 0.toChar()
        for (i in 0..<n) {
            val current = normalized[i]
            // Remove duplicate slashes.
            if (!(current == '/' && prevChar == '/')) {
                normalized[index++] = current
            }

            prevChar = current
        }

        // Omit the trailing slash, except when pathname == "/".
        if (prevChar == '/' && n > 1) {
            index--
        }

        return if (index != n) String(normalized, 0, index) else pathname
    }
}

