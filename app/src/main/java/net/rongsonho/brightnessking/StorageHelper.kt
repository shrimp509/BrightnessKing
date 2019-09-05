package net.rongsonho.brightnessking

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager

private const val STATE_KEY = "activate_state"
private const val ON_PURPOSE_KEY = "start_on_purpose"

class StorageHelper(private val context : Context) {

    @SuppressLint("ApplySharedPref")
    fun setIsActivate(activate : Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(STATE_KEY, activate)
            .commit()
    }

    fun getIsActivate() : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(STATE_KEY, false)
    }

    @SuppressLint("ApplySharedPref")
    fun setOnPurpose(onPurpose : Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(ON_PURPOSE_KEY, onPurpose)
            .commit()
    }

    fun getOnPurpose() : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(ON_PURPOSE_KEY, false)
    }
}