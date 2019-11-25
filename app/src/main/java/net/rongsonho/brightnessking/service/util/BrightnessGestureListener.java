package net.rongsonho.brightnessking.service.util;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class BrightnessGestureListener extends GestureDetector.SimpleOnGestureListener {
    private float lastX = 0, lastY = 0, deltaX = 0, deltaY = 0;
    private Context context;

    public BrightnessGestureListener(Context context) {
        this.context = context;
    }

    /*
     * delta < 0 -> scroll to right
     * delta > 0 -> scroll to left
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // Bottom side
        deltaX = distanceX - lastX;
        setBrightness(context, -deltaX);
        return true;
    }

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
    }

    private void setBrightness(Context context, int brightness) {
        ContentResolver contentResolver = context.getContentResolver();
        if (brightness > 0 && brightness <= 255){
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        }
    }
}
