package net.rongsonho.brightnessking.util

import android.content.Context
import androidx.preference.PreferenceManager
import net.rongsonho.brightnessking.setting.data.Gravity

private const val FIRST_OPEN_KEY = "is_first_open_after_download"
private const val AUTO_RESTART_KEY = "auto_restart_brightness_service"
private const val THICKNESS_KEY = "thickness"
private const val GRAVITY_KEY = "gravity"

class StorageHelper {

    companion object {
        private const val TAG = "StorageHelper"

        // first open
        fun setIsFirstOpenAfterDownload(context : Context, activate : Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(FIRST_OPEN_KEY, activate)
                .apply()
        }

        fun getIsFirstOpenAfterDownload(context : Context) : Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(FIRST_OPEN_KEY, false)
        }

        // Setting: auto restart
        fun setAutoRestart(context: Context, auto: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AUTO_RESTART_KEY, auto)
                .apply()
        }

        fun getAutoRestart(context: Context) : Boolean{
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AUTO_RESTART_KEY, false)
        }

        // Setting: gravity
        fun setGravity(context: Context, gravity: Gravity) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(GRAVITY_KEY, gravity.name)
                .apply()
        }

        fun getGravity(context: Context) : Gravity {
            return Gravity.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(GRAVITY_KEY, Gravity.BOTTOM.name)!!)
        }

        // Setting: thickness
        fun setThickness(context: Context, thickness: Int) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(THICKNESS_KEY, thickness)
                .apply()
        }

        fun getThickness(context: Context) : Int{
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(THICKNESS_KEY, 5)
        }
    }
}