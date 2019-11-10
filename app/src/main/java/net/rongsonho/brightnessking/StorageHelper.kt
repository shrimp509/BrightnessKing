package net.rongsonho.brightnessking

import android.content.Context
import androidx.preference.PreferenceManager

private const val FIRST_OPEN_KEY = "is_first_open_after_download"

class StorageHelper() {

    companion object {
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
    }



}