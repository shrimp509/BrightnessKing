package net.rongsonho.brightnessking.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import net.rongsonho.brightnessking.R

private const val TAG = "SettingFragment"

class SettingFragment : Fragment(), View.OnTouchListener {
    /* **************************
     * Constants
     * **************************/
    companion object {
        private const val SETTING_WHOLE_HEIGHT_RATIO = 0.8f
    }

    /* **************************
     * Global variables
     * **************************/
    private lateinit var rootView: View
    @BindView(R.id.setting_empty_layout_touch_area) lateinit var dragArea: LinearLayout
    private var screenHeight = 0
    private var screenWidth = 0
    private var fragmentHeight = 0

    // touch/drag variable
    private var yDelta = 0f
    private var lastY = 0f
    private var currentFingerY = 0f
    private var spread = false

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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

    // get screen width and height
    private fun getScreenResolution(): Pair<Int, Int>? {
        val display = resources.displayMetrics
        val deviceHeight = display.heightPixels
        val deviceWidth = display.widthPixels
        return Pair(deviceWidth, deviceHeight)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                currentFingerY = event.rawY
                yDelta = currentFingerY - lastY
                Log.d(TAG, "onTouch, y: $currentFingerY")

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
}