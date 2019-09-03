package net.rongsonho.brightnessking

import android.content.Context
import android.preference.PreferenceManager

private const val STATE_KEY = "activate_state"

class StorageHelper(private val context : Context) {

    fun setIsActivate(activate : Boolean){
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(STATE_KEY, activate)
            .apply()
    }

    fun getIsActivate() : Boolean{
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(STATE_KEY, false)
    }
}