package net.rongsonho.brightnessking.util

import net.rongsonho.brightnessking.service.BrightnessService

class Global {

    companion object {
        private const val TAG = "Global"

        /* ****************************
         * Private Variables
         * ****************************/
        private var onGravityChangedListener: BrightnessService.OnGravityChangedListener? = null
        private var onThicknessChangedListener: BrightnessService.OnThicknessChangedListener? = null

        /* ****************************
         * Public APIs
         * ****************************/
        @JvmStatic fun setOnGravityChangedListener(listener: BrightnessService.OnGravityChangedListener) {
            this.onGravityChangedListener = listener
        }

        @JvmStatic fun getOnGravityChangedListener() : BrightnessService.OnGravityChangedListener? {
            return onGravityChangedListener
        }

        @JvmStatic fun setOnThicknessChangedListener(listener: BrightnessService.OnThicknessChangedListener) {
            this.onThicknessChangedListener = listener
        }

        @JvmStatic fun getOnThicknessChangedListener() : BrightnessService.OnThicknessChangedListener? {
            return onThicknessChangedListener
        }
    }
}