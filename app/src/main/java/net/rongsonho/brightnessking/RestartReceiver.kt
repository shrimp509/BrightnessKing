package net.rongsonho.brightnessking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast

private const val TAG = "RestartReceiver"

class RestartReceiver : BroadcastReceiver() {

    override fun onReceive(context : Context?, intent: Intent?) {
        showToast(context, "onReceive, try to restart the service.")
        val brightnessService = Intent(context, BrightnessService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(brightnessService)
        }else {
            context!!.startService(brightnessService)
        }
        showToast(context, "onReceive, restart the service done.")
    }

    private fun showToast(context: Context?, msg : String) {
        Log.d(TAG, msg)
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}