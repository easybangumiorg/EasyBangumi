package com.heyanle.eplayer_standard.utils

/**
 * Create by heyanlin on 2022/11/7
 */
class FigureCounter {

    var max = 1.0f
    var min = 0.0f

    var outMax = 100
    var outMin = 0


    var deltaSum = 0.0f



    fun add(delta: Float): Int{
        deltaSum += delta

        val curDelta = ((deltaSum/(max-min))*(outMax - outMin)).toInt()
        return if(curDelta == 0){
            0
        }else{
            deltaSum = 0f
            curDelta
        }

    }



}