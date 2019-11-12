package net.rongsonho.brightnessking

import android.animation.Animator
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.app.ActivityManager
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RC_WRITE_SETTING = 0
private const val RC_SYSTEM_OVERLAY = 1
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var btn : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()

        initView()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun initView() {
        initBrightnessOnOffButton()
        setLogoAnimation()
        showTutorial()
    }

    private fun initBrightnessOnOffButton() {
        val title = findViewById<TextView>(R.id.main_title)
        title.setOnClickListener {
            Log.d(TAG, "is my service running?: ${isMyServiceRunning()}")
        }

        btn = findViewById(R.id.main_btn)

        // update button state first
        btn.setImageResource(
            if (isMyServiceRunning()) R.drawable.button_on_state
            else R.drawable.button_off_state
        )

        // set action
        btn.setOnClickListener {
            setButtonBehavior()
        }
    }

    private fun setLogoAnimation() {

        // set animation
        val backgroundWhite = findViewById<ImageView>(R.id.white_background)
        val logo = findViewById<ImageView>(R.id.main_icon)

        logo.animate().alpha(1f).setDuration(2000).setListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {
                // Nothing to do
            }

            override fun onAnimationEnd(p0: Animator?) {

                // set Foreground icon and background to
                backgroundWhite.animate().alpha(0f).setDuration(300).start()
                logo.animate().alpha(0f).setDuration(300).start()
            }

            override fun onAnimationCancel(p0: Animator?) {
                // Nothing to do
            }

            override fun onAnimationStart(p0: Animator?) {
                // Nothing to do
            }
        }).start()
    }

    private fun checkPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
                startActivityForResult(intent, RC_WRITE_SETTING)
            }

            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, RC_SYSTEM_OVERLAY)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.main_alert_dialog_title_permissions_denied))
                .setMessage(resources.getString(R.string.main_alert_dialog_message_permissions_denied))
                .setPositiveButton(resources.getString(R.string.main_alert_dialog_ok_permissions_denied)) { _ , _ ->
                    checkPermissions()
                } .create()
                .show()
        }
    }

    private fun showToast(msg: String) {
       Log.d(TAG, "toast: $msg")
       Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun setButtonBehavior() {
        val brightnessService = Intent(this, BrightnessService::class.java)
        if (isMyServiceRunning()) {
            // try to close service
            CoroutineScope(Dispatchers.Main).launch {
                delay(50)
                val close = stopService(brightnessService)

                if (!close) {
                    while (isMyServiceRunning()) {
                        Thread.sleep(500)
                        Log.d(TAG, "service isn't close yet")
                        stopService(brightnessService)
                        btn.setImageResource(R.drawable.button_off_state)
                    }
                }else {
                    btn.setImageResource(R.drawable.button_off_state)
                }
            }
        }else {
            // disable auto brightness control
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this)) {
//                showToast("替您關閉手機設定中的自動調整亮度哦")
//                Settings.System.putInt(contentResolver, SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL)    // This can disable auto-brightness
//            }

            // try to start service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(brightnessService)
            }else {
                startService(brightnessService)
            }

            // check service is running or not
            if (isMyServiceRunning()) {
                btn.setImageResource(R.drawable.button_on_state)
            }else {
                showToast("service isn't started")
            }
        }
    }

    private fun isMyServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d(TAG, "service name: ${service.service.className}")
            if (BrightnessService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun showTutorial() {
        if (!StorageHelper.getIsFirstOpenAfterDownload(this)) {
            // show welcome dialog
            AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.main_welcome_dialog_title))
                .setMessage(resources.getString(R.string.main_welcome_dialog_message))
                .setPositiveButton(resources.getString(R.string.main_welcome_dialog_positive_btn), null)
                .create()
                .show()

            // TODO: show tutorial fragments

            // set preference value to true
            StorageHelper.setIsFirstOpenAfterDownload(this, true)
        }
    }
}
