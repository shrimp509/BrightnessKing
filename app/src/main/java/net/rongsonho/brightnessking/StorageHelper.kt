package net.rongsonho.brightnessking

import android.content.Context
import android.content.Context.MODE_PRIVATE

private const val FILE_NAME = "shared_preference"
private const val STATE_KEY = "activate_state"

class StorageHelper(context : Context) {
    private val context = context

    fun setIsActivate(activate : Boolean){
        context.getSharedPreferences(FILE_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(STATE_KEY, activate)
            .apply()
    }

    fun getIsActivate() : Boolean{
        return context.getSharedPreferences(FILE_NAME, MODE_PRIVATE)
            .getBoolean(STATE_KEY, false)
    }
}