package net.rongsonho.brightnessking.service.util;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import net.rongsonho.brightnessking.setting.data.Gravity;
import net.rongsonho.brightnessking.util.StorageHelper;

public class BrightnessGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final String TAG = BrightnessGestureListener.class.getSimpleName();
    private static final int MAX_BRIGHTNESS_NORMAL = 255;
    private static final int BRIGHTNESS_MULTIPLIER_NORMAL = 1;
    private static final int MAX_BRIGHTNESS_DELTA_NORMAL = 10;

    private static final int MAX_BRIGHTNESS_XIAOMI = 4000;
    private static final int BRIGHTNESS_MULTIPLIER_XIAOMI = 16;    // 4000/255 = 15.x
    private static final int MAX_BRIGHTNESS_DELTA_XIAOMI = MAX_BRIGHTNESS_DELTA_NORMAL * BRIGHTNESS_MULTIPLIER_XIAOMI;

    private float deltaX = 0, deltaY = 0;
    private Context context;
    private Gravity gravity;

    public BrightnessGestureListener(Context context, Gravity gravity) {
        this.context = context;
        this.gravity = gravity;
    }

    /*
     * Gravity: TOP and BOTTOM
     *  delta < 0 -> scroll to right
     *  delta > 0 -> scroll to left
     *
     * Gravity: LEFT and RIGHT
     *  delta > 0 -> scroll to up
     *  delta < 0 -> scroll to down
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        switch (gravity) {
            case TOP:
            case BOTTOM:
                Log.d(TAG, "top/bottom, adjust brightness, " + deltaX);
                deltaX = distanceX;
                setBrightness(context, -deltaX);
                break;

            case LEFT:
            case RIGHT:
                deltaY = distanceY;
                setBrightness(context, deltaY);
                break;
        }
        return true;
    }

    /*
     * Set relative brightness value
     */
    private void setBrightness(Context context, float brightnessDelta) {
        ContentResolver contentResolver = context.getContentResolver();

        // get parameter brightness with different devices
        int multiplier = getMultiplierWithManufacturer();
        int maxBrightness = getMaxBrightnessWithManufacturer();
        int maxDelta = getMaxDeltaWithManufacturer();

        // brightness calculation
        int changedBrightness = getBrightness(contentResolver, brightnessDelta,
                multiplier, maxDelta);

        // actually adjust brightness through system api
        if (changedBrightness > 0 && changedBrightness <= maxBrightness) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, changedBrightness);
        }

        // vibrate
        vibrateWithBrightnessLevel(changedBrightness, maxBrightness);
    }


    public void setGravity(Gravity gravity) {
        this.gravity = gravity;
    }

    /*
     * Set vibration
     */
    private void vibrate(int millis) {
        if (StorageHelper.getVibration(context)) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(millis);
            }
        }
    }

    private void vibrateWithBrightnessLevel(int brightness, int maxBrightness) {
        // vibrate with brightness level
        if (brightness >  maxBrightness) {
            vibrate(200);
        } else if (brightness >= (maxBrightness * 0.8) && brightness < maxBrightness) {
            vibrate(20);
        } else if (brightness >= (maxBrightness * 0.6) && brightness < (maxBrightness * 0.8)) {
            vibrate(15);
        } else if (brightness >= (maxBrightness * 0.4) && brightness < (maxBrightness * 0.6)) {
            vibrate(10);
        } else if (brightness >= (maxBrightness * 0.2) && brightness < (maxBrightness * 0.4)) {
            vibrate(8);
        } else if (brightness < (maxBrightness * 0.2) && brightness >= 0) {
            vibrate(5);
        } else if (brightness < 0) {
            vibrate(200);
        }
    }

    private int getMultiplierWithManufacturer() {
        if ("xiaomi".equals(Build.MANUFACTURER.toLowerCase())) {
            return BRIGHTNESS_MULTIPLIER_XIAOMI;
        }

        return BRIGHTNESS_MULTIPLIER_NORMAL;
    }

    private int getMaxBrightnessWithManufacturer() {
        if ("xiaomi".equals(Build.MANUFACTURER.toLowerCase())) {
            return MAX_BRIGHTNESS_XIAOMI;
        }

        return MAX_BRIGHTNESS_NORMAL;
    }

    private int getMaxDeltaWithManufacturer() {
        if ("xiaomi".equals(Build.MANUFACTURER.toLowerCase())) {
            return MAX_BRIGHTNESS_DELTA_XIAOMI;
        }

        return MAX_BRIGHTNESS_DELTA_NORMAL;
    }

    private float getCurrentBrightness(ContentResolver contentResolver) {
        float nowBrightness = 0;
        try{
            nowBrightness = Settings.System.getFloat(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        }catch (Exception e){
            e.printStackTrace();
        }
        return nowBrightness;
    }

    private int getBrightness(ContentResolver contentResolver, float brightnessDelta, int multiplier, int maxDelta) {
        float nowBrightness = getCurrentBrightness(contentResolver);

        brightnessDelta *= multiplier;

        // avoid adjusting brightness too much
        if (brightnessDelta > maxDelta) {
            brightnessDelta = maxDelta;
        }

        return (int)(nowBrightness + brightnessDelta);
    }
}
