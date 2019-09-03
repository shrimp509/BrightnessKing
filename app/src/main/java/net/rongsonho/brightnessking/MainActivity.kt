package net.rongsonho.brightnessking

import android.animation.Animator
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

private const val RC_WRITE_SETTING = 0
private const val RC_SYSTEM_OVERLAY = 1
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var btn : ImageButton
    private val storageHelper = StorageHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()

        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initView(){
        initBrightnessOnOffButton()

        setLogoAnimation()

        setTitleTypeFace()
    }

    private fun initBrightnessOnOffButton(){
        btn = findViewById(R.id.main_btn)

        // update button state first
        if (storageHelper.getIsActivate()) {
            btn.setImageResource(R.drawable.button_on_state)
        }else {
            btn.setImageResource(R.drawable.button_off_state)
        }

        // set action
        val brightnessService = Intent(this, BrightnessService::class.java)
        btn.setOnClickListener {
            if (storageHelper.getIsActivate()) {
                stopService(brightnessService)
                btn.setImageResource(R.drawable.button_off_state)
            }else {
                startService(brightnessService)
                btn.setImageResource(R.drawable.button_on_state)
            }
        }
    }

    private fun setLogoAnimation(){

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

    private fun setTitleTypeFace(){
        val title = findViewById<TextView>(R.id.main_title)
        setTypeFace(title)
    }

    private fun setTypeFace(view : TextView){
        view.typeface = Typeface.createFromAsset(assets, resources.getString(R.string.main_title_typeface))
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

    private fun bindService() {
        Log.d(TAG, "bindService")
        val intent = Intent(this, BrightnessService::class.java)
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        startService(intent)
    }

    private fun unbindService() {
        Log.d(TAG, "unbindService")
        unbindService(serviceConnection)
    }

    private val serviceConnection = object : ServiceConnection{
        override fun onServiceDisconnected(p0: ComponentName?) {
            storageHelper.setIsActivate(false)
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            storageHelper.setIsActivate(true)
        }

    }
}
