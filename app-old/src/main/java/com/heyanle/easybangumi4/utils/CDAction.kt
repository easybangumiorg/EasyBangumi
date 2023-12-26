package com.heyanle.easybangumi4.utils

/**
 * Created by HeYanLe on 2023/3/10 20:03.
 * https://github.com/heyanLE
 */
class CDAction(
    private val cd: Int = 300,
) {

    private var lastTime = -1L

    fun action(block: ()->Unit){
        val now = System.currentTimeMillis()
        if(lastTime < 0 || now - lastTime >= cd){
            lastTime = now
            block()
        }
    }




}