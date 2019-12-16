package net.rongsonho.brightnessking.ui

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
import android.graphics.Color
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.rongsonho.brightnessking.R
import net.rongsonho.brightnessking.util.StorageHelper
import net.rongsonho.brightnessking.service.BrightnessService
import org.w3c.dom.Text

private const val RC_WRITE_SETTING = 0
private const val RC_SYSTEM_OVERLAY = 1
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    @BindView(R.id.main_btn) lateinit var bulbBtn : ImageButton
    @BindView(R.id.bulb_background_layer2) lateinit var bulbBackgroundLayer2 : ImageView
    @BindView(R.id.bulb_background_layer3) lateinit var bulbBackgroundLayer3 : ImageView
    @BindView(R.id.on_background) lateinit var stateOnBackground : ImageView
    @BindView(R.id.off_background) lateinit var stateOffBackground : ImageView
    @BindView(R.id.main_title) lateinit var title : TextView
    @BindView(R.id.main_subtitle) lateinit var subtitle : TextView

            override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_version2)

        ButterKnife.bind(this)

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

        // update button state first
        if (isMyServiceRunning()) {
            initLightOn()
        } else {
            initLightOff()
        }

        // set action
        bulbBtn.setOnClickListener {
            setButtonBehavior()
        }
    }

    private fun setLogoAnimation() {
        // set animation
        val backgroundWhite = findViewById<ImageView>(R.id.white_background)
        val logo = findViewById<ImageView>(R.id.main_logo)

        // show logo and disappear
        logo.animate().alpha(1f).setDuration(2000).withEndAction {
            // set Foreground icon and background to invisible
            backgroundWhite.animate().alpha(0f).setDuration(300).start()
            logo.animate().alpha(0f).setDuration(300).start()
        }.start()
    }

    private fun checkPermissions() {
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
                .setPositiveButton(resources.getString(R.string.main_alert_dialog_ok_permissions_denied)) { _, _ ->
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
        if (isMyServiceRunning()) {
            turnOffService()
        }else {
            turnOnService()
        }
    }

    private fun turnOffService() {
        // try to close service
        CoroutineScope(Dispatchers.Main).launch {
            val brightnessService = Intent(this@MainActivity, BrightnessService::class.java)
            delay(50)
            val close = stopService(brightnessService)

            if (!close) {
                while (isMyServiceRunning()) {
                    Log.d(TAG, "service isn't closed yet.")
                    stopService(brightnessService)
                }

                animateTurnOffTheLight()
            }else {
                Log.d(TAG, "service is closed.")

                animateTurnOffTheLight()
            }
        }
    }

    private fun turnOnService() {
        val brightnessService = Intent(this, BrightnessService::class.java)

        // disable auto brightness control
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this)) {
//            showToast("替您關閉手機設定中的自動調整亮度哦")
//            Settings.System.putInt(contentResolver, SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL)    // This can disable auto-brightness
//        }

        // try to start service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(brightnessService)
        }else {
            startService(brightnessService)
        }

        // auto close app after starting service
        CoroutineScope(Dispatchers.Main).launch {
            // check service is running or not
            if (isMyServiceRunning()) {
                CoroutineScope(Dispatchers.Main).launch {
                    animateTurnOnTheLight()
                }
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

    private suspend fun animateTurnOnTheLight() {
        title.setTextColor(Color.parseColor("#393737"))
        subtitle.setTextColor(Color.parseColor("#919497"))
        stateOffBackground.animate().alpha(0f).setDuration(3000).start()
        bulbBtn.setImageResource(R.drawable.button_on_state)
        delay(400)
        bulbBackgroundLayer2.animate().setDuration(1000).alpha(1f).start()
        delay(400)
        bulbBackgroundLayer3.animate().setDuration(1000).alpha(1f).start()
    }

    private suspend fun animateTurnOffTheLight() {
        title.setTextColor(Color.parseColor("#FAFAFA"))
        subtitle.setTextColor(Color.parseColor("#919497"))
        bulbBtn.setImageResource(R.drawable.button_off_state)
        stateOffBackground.animate().alpha(1f).setDuration(3000).start()
        bulbBackgroundLayer3.animate().alpha(0f).setDuration(800).start()
        delay(500)
        bulbBackgroundLayer2.animate().alpha(0f).setDuration(800).start()
    }

    private fun initLightOn() {
        title.setTextColor(Color.parseColor("#393737"))
        subtitle.setTextColor(Color.parseColor("#919497"))
        stateOffBackground.alpha = 0f
        bulbBtn.setImageResource(R.drawable.button_on_state)
        bulbBackgroundLayer2.alpha = 1f
        bulbBackgroundLayer3.alpha = 1f
    }

    private fun initLightOff() {
        title.setTextColor(Color.parseColor("#FAFAFA"))
        subtitle.setTextColor(Color.parseColor("#919497"))
        stateOffBackground.alpha = 1f
        bulbBtn.setImageResource(R.drawable.button_off_state)
        bulbBackgroundLayer2.alpha = 0f
        bulbBackgroundLayer3.alpha = 0f
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
            StorageHelper.setIsFirstOpenAfterDownload(
                this,
                true
            )
        }
    }
}
