package com.heyanle.easybangumi4.utils

import com.heyanle.okkv2.core.okkv
import java.util.UUID

/**
 * Created by heyanlin on 2024/2/13 21:18.
 */
object UUIDHelper {


    fun getUUID(): String {
        var uuid by okkv<String>("easy_bangumi_uuid")
        val uu = uuid
        return if (uu == null) {
            val u = UUID.randomUUID().toString()
            uuid = u
            u
        } else {
            uu
        }
    }

}