package net.rongsonho.brightnessking.setting

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import net.rongsonho.brightnessking.BuildConfig
import net.rongsonho.brightnessking.R
import net.rongsonho.brightnessking.service.BrightnessService
import net.rongsonho.brightnessking.setting.ParametersCalculator.Companion.getThickness
import net.rongsonho.brightnessking.setting.data.Gravity
import net.rongsonho.brightnessking.util.Global
import net.rongsonho.brightnessking.util.StorageHelper

private const val TAG = "SettingFragment"

class SettingFragment : Fragment(), View.OnTouchListener {

    /* **************************
     * Constants
     * **************************/
    companion object {
        private const val SETTING_WHOLE_HEIGHT_RATIO = 0.8f
        private const val REPORT_URL = "https://docs.google.com/forms/d/e/1FAIpQLSfxYMs4A3Q2kg8zDV0c2iLQQZ3Ex2n-RZ1ESUuwHoKZoLtlyQ/viewform?"
    }

    /* **************************
     * Global variables
     * **************************/
    // touch/drag variable
    @BindView(R.id.setting_empty_layout_touch_area) lateinit var dragArea: LinearLayout
    private lateinit var rootView: View
    private var screenHeight = 0
    private var screenWidth = 0
    private var fragmentHeight = 0
    private var yDelta = 0f
    private var lastY = 0f
    private var currentFingerY = 0f
    private var spread = false

    // setting items
    @BindView(R.id.setting_item_auto_restart_item_switch) lateinit var autoRestartSwitch : SwitchCompat
    @BindView(R.id.setting_item_choose_gravity_item_spinner) lateinit var gravitySpinner : Spinner
    @BindView(R.id.setting_item_adjust_thickness_item_seekbar) lateinit var thicknessBar : SeekBar
    @BindView(R.id.setting_item_vibration_item_switch) lateinit var vibrationSwitch: SwitchCompat
    @BindView(R.id.setting_item_report_item_goto) lateinit var reportBtn: TextView

    /* **************************
     * Initialization
     * **************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ButterKnife.bind(this, view)
        initView(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(view: View) {
        rootView = view
        dragArea.setOnTouchListener(this)

        // check setting items' state
        initPrefStates()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // get screen size
        try {
            val screenSize = getScreenResolution()
            if (screenSize != null) {
                val params = rootView.layoutParams
                fragmentHeight = (getScreenResolution()!!.second * SETTING_WHOLE_HEIGHT_RATIO).toInt()
                params.height = fragmentHeight
                rootView.layoutParams = params

                // set screen size
                screenWidth = screenSize.first
                screenHeight = screenSize.second
            }
        } catch (e: Exception) {
            Log.d(TAG, "SettingFragment Height Exception: $e")
        }
    }

    /* **************************
     * General methods
     * **************************/
    // Get screen width and height
    private fun getScreenResolution(): Pair<Int, Int>? {
        val display = resources.displayMetrics
        val deviceHeight = display.heightPixels
        val deviceWidth = display.widthPixels
        return Pair(deviceWidth, deviceHeight)
    }

    // Define the touch movement of setting panel
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                currentFingerY = event.rawY
                yDelta = currentFingerY - lastY
//                Log.d(TAG, "onTouch, y: $currentFingerY")

                if (currentFingerY >= screenHeight * (1 - SETTING_WHOLE_HEIGHT_RATIO + 0.05f)
                    && currentFingerY <= screenHeight * (1 - SETTING_WHOLE_HEIGHT_RATIO * 0.1f)) {

                    if (rootView.y < screenHeight * (1 - SETTING_WHOLE_HEIGHT_RATIO + 0.05f)) {
                        rootView.y = screenHeight * (1 - SETTING_WHOLE_HEIGHT_RATIO + 0.05f)
                    }else {
                        rootView.animate().yBy(yDelta).setDuration(0).start()
                    }
                }
                lastY = currentFingerY
            }
            MotionEvent.ACTION_UP -> {
                if (yDelta < 0) {
                    rootView.animate().y(screenHeight * (1 - SETTING_WHOLE_HEIGHT_RATIO + 0.05f))
                        .setInterpolator(OvershootInterpolator(1f)).setDuration(300).start()
                    spread = true
                } else {
                    rootView.animate().y( screenHeight * (1 - SETTING_WHOLE_HEIGHT_RATIO * 0.1f))
                        .setInterpolator(OvershootInterpolator(1f)).setDuration(300).start()
                    spread = false
                }
            }
        }
        return true
    }

    // Check every setting item's state (on or off, stored values)
    private fun initPrefStates() {
        // init auto restart
        autoRestartSwitch.isChecked = StorageHelper.getAutoRestart(context!!)

        // set auto restart switch listener
        autoRestartSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            StorageHelper.setAutoRestart(context!!, isChecked)
        }

        // set gravity spinner
        val gravityList = arrayListOf("下方", "右方", "左方", "上方")
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, gravityList)
        gravitySpinner.adapter = adapter
        gravitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}    // do nothing
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // set spinner text color
                (parent!!.getChildAt(0) as TextView).setTextColor(Color.parseColor("#2A2C2E"))

                // update the gravity
                StorageHelper.setGravity(context!!, positionToGravity(position))

                // really change gravity
                if (Global.getOnGravityChangedListener() != null && isMyServiceRunning()) {
                    Global.getOnGravityChangedListener()?.setGravity(positionToGravity(position))
                }
            }
        }

        // init gravity
        gravitySpinner.setSelection(
            gravityToPosition(
                StorageHelper.getGravity(context!!)
            )
        )

        // init thickness seekbar
        thicknessBar.progress = StorageHelper.getThicknessProgress(context!!)

        // set seekbar style (thumb color and progress color)
        thicknessBar.progressDrawable.setColorFilter(Color.parseColor("#A1A3A1"), PorterDuff.Mode.MULTIPLY)
        thicknessBar.thumb.setColorFilter(Color.parseColor("#45494C"), PorterDuff.Mode.SRC_IN)

        // set thickness seekbar listener
        thicknessBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                StorageHelper.setThicknessProgress(context!!, progress)

                if (isMyServiceRunning()) {
                    Global.getOnThicknessChangedListener()?.setThickness(
                        getThickness(getScreenResolution()!!, StorageHelper.getGravity(context!!), progress)
                    )
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // init vibration
        vibrationSwitch.isChecked = StorageHelper.getVibration(context!!)

        // set vibration switch listener
        vibrationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            StorageHelper.setVibration(context!!, isChecked)
        }

        // set report btn
        reportBtn.setOnClickListener {
            Toast.makeText(context, "正在打開瀏覽器...", Toast.LENGTH_SHORT).show()

            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(REPORT_URL + "entry.11547474=${BuildConfig.VERSION_NAME}&entry.1484421761=${android.os.Build.VERSION.SDK_INT}&entry.1788770952=${Build.MANUFACTURER} ${Build.MODEL}")
            startActivity(openURL)
        }
    }

    private fun gravityToPosition(gravity: Gravity) : Int {
        return when(gravity) {
            Gravity.BOTTOM -> 0
            Gravity.RIGHT -> 1
            Gravity.LEFT -> 2
            Gravity.TOP -> 3
        }
    }

    private fun positionToGravity(position: Int) : Gravity {
        return when (position) {
            0 -> Gravity.BOTTOM
            1 -> Gravity.RIGHT
            2 -> Gravity.LEFT
            3 -> Gravity.TOP
            else -> Gravity.BOTTOM
        }
    }

    private fun isMyServiceRunning(): Boolean {
        val manager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d(TAG, "service name: ${service.service.className}")
            if (BrightnessService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}