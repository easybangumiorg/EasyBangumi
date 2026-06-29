package org.easybangumi.next.shared.playcon

object TimeUtils {

    fun toString(ti: Long): String {
        val time = (ti / 1000).toInt()
        val min = (time / 60).toInt()
        val se = (time % 60).toInt()

        return "${if (min >= 10) min else "0$min"}:${if (se >= 10) se else "0$se"}"
    }

}