package net.rongsonho.brightnessking.service

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast

private const val TAG = "RestartReceiver"

class RestartReceiver : BroadcastReceiver() {

    override fun onReceive(context : Context?, intent: Intent?) {
        val brightnessService = Intent(context, BrightnessService::class.java)

        if ("android.intent.action.BOOT_COMPLETED" == intent!!.action) {
            // check if service is running
            if (!isMyServiceRunning(context!!)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(brightnessService)
                }else {
                    context.startService(brightnessService)
                }
            }
            showToast(context, "重新開啟不完美的亮度王中...")
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