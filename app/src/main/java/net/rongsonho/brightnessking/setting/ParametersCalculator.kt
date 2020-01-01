package net.rongsonho.brightnessking.setting

import net.rongsonho.brightnessking.setting.data.Gravity
import android.util.Pair

class ParametersCalculator {
    companion object {
        @JvmStatic fun getThickness(screenSize: Pair<Int, Int>, gravity: Gravity, progress: Int) : Int{
            val checkedProgress = if (progress in 0 .. 100) progress else 75

            return when (gravity) {
                Gravity.TOP, Gravity.BOTTOM -> {
                    val height = screenSize.second.toFloat()
                    ((checkedProgress/100f) * (height/27 - height/60) + height/60).toInt()
                }
                Gravity.LEFT, Gravity.RIGHT -> {
                    val width = screenSize.first.toFloat()
                    ((checkedProgress/100f) * (width/13 - width/30) + width/30).toInt()
                }
            }
        }
    }
}