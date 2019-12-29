package net.rongsonho.brightnessking.service

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import net.rongsonho.brightnessking.util.StorageHelper

private const val TAG = "RestartReceiver"

class RestartReceiver : BroadcastReceiver() {

    override fun onReceive(context : Context?, intent: Intent?) {

        if ("android.intent.action.BOOT_COMPLETED" == intent!!.action) {
            // check setting
            if (StorageHelper.getAutoRestart(context!!)) {
                // check if service is running
                if (!isMyServiceRunning(context)) {
                    val brightnessService = Intent(context, BrightnessService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(brightnessService)
                    } else {
                        context.startService(brightnessService)
                    }
                }
                showToast(context, "重新開啟不完美的亮度王中...")
            } else {
                Log.d(TAG, "Setting is not triggered to auto restart.")
            }
        } else {
            Log.d(TAG, "action is not == android.intent.action.BOOT_COMPLETED")
        }
    }

    private fun showToast(context: Context?, msg : String) {
        Log.d(TAG, msg)
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    private fun isMyServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d(TAG, "service name: ${service.service.className}")
            if (BrightnessService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}