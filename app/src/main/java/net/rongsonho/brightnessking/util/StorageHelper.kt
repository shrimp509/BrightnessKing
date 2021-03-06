package net.rongsonho.brightnessking.util

import android.content.Context
import androidx.preference.PreferenceManager
import net.rongsonho.brightnessking.setting.data.Gravity

private const val FIRST_OPEN_KEY = "is_first_open_after_download"
private const val AUTO_RESTART_KEY = "auto_restart_brightness_service"
private const val THICKNESS_KEY = "thickness"
private const val GRAVITY_KEY = "gravity"
private const val VIBRATION_KEY = "vibration"

class StorageHelper {

    companion object {
        private const val TAG = "StorageHelper"

        // Cache of vibration setting
        private var vibrationCache = false

        @JvmStatic
        // first open
        fun setIsFirstOpenAfterDownload(context : Context, activate : Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(FIRST_OPEN_KEY, activate)
                .apply()
        }

        @JvmStatic
        fun getIsFirstOpenAfterDownload(context : Context) : Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(FIRST_OPEN_KEY, false)
        }

        @JvmStatic
        // Setting: auto restart
        fun setAutoRestart(context: Context, auto: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AUTO_RESTART_KEY, auto)
                .apply()
        }

        @JvmStatic
        fun getAutoRestart(context: Context) : Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AUTO_RESTART_KEY, false)
        }

        @JvmStatic
        // Setting: gravity
        fun setGravity(context: Context, gravity: Gravity) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(GRAVITY_KEY, gravity.name)
                .apply()
        }

        @JvmStatic
        fun getGravity(context: Context) : Gravity {
            return Gravity.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(GRAVITY_KEY, Gravity.BOTTOM.name)!!)
        }

        @JvmStatic
        // Setting: thickness
        fun setThicknessProgress(context: Context, thickness: Int) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(THICKNESS_KEY, thickness)
                .apply()
        }

        @JvmStatic
        fun getThicknessProgress(context: Context) : Int {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(THICKNESS_KEY, 5)
        }

        @JvmStatic
        // Setting: Vibration
        fun setVibration(context: Context, enable: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(VIBRATION_KEY, enable)
                .apply()

            vibrationCache = enable
        }

        @JvmStatic
        fun getVibration(context: Context) : Boolean {
            return vibrationCache
//            return PreferenceManager.getDefaultSharedPreferences(context)
//                .getBoolean(VIBRATION_KEY, false)
        }

    }
}