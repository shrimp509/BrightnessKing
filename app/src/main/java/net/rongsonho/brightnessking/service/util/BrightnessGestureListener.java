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
        float nowBrightness = 0;
        try{
            nowBrightness = Settings.System.getFloat(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        }catch (Exception e){
            e.printStackTrace();
        }

        // avoid adjusting brightness too much
        if (brightnessDelta > 10){
            brightnessDelta = 10;
        }

        int changedBrightness = (int)(nowBrightness + brightnessDelta);

        if (changedBrightness > 0 && changedBrightness <= 255){
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, changedBrightness);
        }

        // vibrate with brightness level
        Log.d(TAG, "current brightness: " + changedBrightness);
        if (changedBrightness >  255) {
            vibrate(200);
        } else if (changedBrightness >= 200 && changedBrightness < 255) {
            vibrate(20);
        } else if (changedBrightness >= 150 && changedBrightness < 200) {
            vibrate(15);
        } else if (changedBrightness >= 100 && changedBrightness < 150) {
            vibrate(10);
        } else if (changedBrightness >= 50 && changedBrightness < 100) {
            vibrate(8);
        } else if (changedBrightness < 50 && changedBrightness >= 0) {
            vibrate(5);
        } else if (changedBrightness < 0) {
            vibrate(200);
        }
    }

    /*
     * Set exactly brightness value
     */
    private void setBrightness(Context context, int brightness) {
        ContentResolver contentResolver = context.getContentResolver();
        if (brightness > 0 && brightness <= 255){
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        }
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
}
