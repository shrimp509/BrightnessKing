package net.rongsonho.brightnessking.util

import net.rongsonho.brightnessking.service.BrightnessService

class Global {

    companion object {
        private const val TAG = "Global"

        /* ****************************
         * Private Variables
         * ****************************/
        private var onGravityChangedListener: BrightnessService.OnGravityChangedListener? = null

        /* ****************************
         * Public APIs
         * ****************************/
        @JvmStatic fun setOnGravityChangedListener(listener: BrightnessService.OnGravityChangedListener) {
            this.onGravityChangedListener = listener
        }

        @JvmStatic fun getOnGravityChangedListener() : BrightnessService.OnGravityChangedListener? {
            return onGravityChangedListener
        }
    }
}