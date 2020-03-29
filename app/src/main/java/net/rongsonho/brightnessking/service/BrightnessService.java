package net.rongsonho.brightnessking.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.*;
import android.widget.LinearLayout;
import androidx.core.app.NotificationCompat;

import net.rongsonho.brightnessking.R;
import net.rongsonho.brightnessking.service.util.BrightnessGestureListener;
import net.rongsonho.brightnessking.setting.ParametersCalculator;
import net.rongsonho.brightnessking.util.Global;
import net.rongsonho.brightnessking.util.StorageHelper;

public class BrightnessService extends Service {
    /* ******************
     * Constants
     * ******************/
    private static final String TAG = "BrightnessService";

    /* ******************
     * Global variables
     * ******************/
    private WindowManager mWindowManager;
    private LinearLayout mSlidingRegion;
    private GestureDetector mGestureDetector;

    // Pass touch event to gesture detector for tuning brightness
    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener mTouchListener = (v, event) -> mGestureDetector.onTouchEvent(event);

    /* ******************
     * Initialization
     * ******************/
    @Override
    public void onCreate() {
        super.onCreate();

        createSlidingRegion();

        // set listeners
        Global.setOnGravityChangedListener(BrightnessService.this::setGravity);
        Global.setOnThicknessChangedListener(thickness -> {
            BrightnessService.this.setThickness(StorageHelper.getGravity(BrightnessService.this), thickness);
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // clear the block
        if (mSlidingRegion != null) {
            // remove view
            mWindowManager.removeView(mSlidingRegion);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind, binder: null");
        return null;
    }

    /* ******************
     * General methods
     * ******************/
    @SuppressLint("ClickableViewAccessibility")
    private void createSlidingRegion() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // get sliding region size
        net.rongsonho.brightnessking.setting.data.Gravity gravity = StorageHelper.getGravity(this);
        Pair<Integer, Integer> regionSize = getSlidingRegionSize(gravity);
        int regionWidth = regionSize.first;
        int regionHeight = regionSize.second;
        Log.d(TAG, "width: " + getScreenResolution().first + ", height: " + getScreenResolution().second +
                ", region width: " + regionWidth + ", region height: " + regionHeight);

        // create rect region
        mSlidingRegion = getSlidingRegion(regionWidth, regionHeight, Color.LTGRAY, 0.2f);

        // draw rect
        mWindowManager.addView(mSlidingRegion, getWindowLayoutParams(regionWidth, regionHeight, fromGravityToGravity(gravity)));

        // set sliding behavior on rect
        mGestureDetector = new GestureDetector(this, new BrightnessGestureListener(this, gravity));
        mSlidingRegion.setOnTouchListener(mTouchListener);

        // set notification channel
        setNotificationChannel();
    }

    private WindowManager.LayoutParams getWindowLayoutParams(int width, int height, int gravity) {
        final WindowManager.LayoutParams windowParams;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowParams = new WindowManager.LayoutParams(width, height,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            windowParams = new WindowManager.LayoutParams(width, height,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        windowParams.gravity = gravity;
        windowParams.x = 0;
        windowParams.y = 0;

        // set window animation
        switch (StorageHelper.getGravity(this)) {
            case TOP:
                windowParams.windowAnimations = R.style.ServiceInAndOutAnimation_Top;
                break;
            case LEFT:
                windowParams.windowAnimations = R.style.ServiceInAndOutAnimation_LEFT;
                break;
            case RIGHT:
                windowParams.windowAnimations = R.style.ServiceInAndOutAnimation_Right;
                break;
            default:
                windowParams.windowAnimations = R.style.ServiceInAndOutAnimation_Bottom;

        }

        return windowParams;
    }

    // get screen width and height
    private Pair<Integer, Integer> getScreenResolution() {
        if (getResources() != null) {
            DisplayMetrics display = getResources().getDisplayMetrics();
            int deviceHeight = display.heightPixels;
            int deviceWidth = display.widthPixels;
            return new Pair<>(deviceWidth, deviceHeight);
        } else {
            return new Pair<>(0, 0);
        }
    }

    private Pair<Integer, Integer> getSlidingRegionSize(net.rongsonho.brightnessking.setting.data.Gravity gravity) {
        Pair<Integer, Integer> screenSize = getScreenResolution();
        int screenWidth = screenSize.first;
        int screenHeight = screenSize.second;
        int thickness = ParametersCalculator.getThickness(
                screenSize,
                gravity,
                StorageHelper.getThicknessProgress(this)
        );
        switch (gravity) {
            case LEFT:
            case RIGHT:
                return new Pair<>(thickness, screenHeight);
            case TOP:
            case BOTTOM:
                return new Pair<>(screenWidth, thickness);
        }
        return new Pair<>(screenWidth, screenHeight/30);
    }

    private LinearLayout getSlidingRegion(int regionWidth, int regionHeight, int color, float alpha) {
        // create, and set color, alpha value
        LinearLayout slidingRegion = new LinearLayout(this);
        slidingRegion.setBackgroundColor(color);
        slidingRegion.setAlpha(alpha);

        // set rect size
        LinearLayout.LayoutParams layoutParams;
        if (regionHeight != 0 || regionWidth != 0) {
            layoutParams = new LinearLayout.LayoutParams(regionWidth, regionHeight);
        } else {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400);
        }
        slidingRegion.setLayoutParams(layoutParams);

        return slidingRegion;
    }

    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // notification channel
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "顯示通知",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

            // compat
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("mContentTitle")
                    .setContentText("mContentText").build();

            startForeground(1, notification);
        }
    }

    private void setGravity(net.rongsonho.brightnessking.setting.data.Gravity gravity) {
        mWindowManager.updateViewLayout(
                mSlidingRegion,
                getWindowLayoutParams(
                        getSlidingRegionSize(gravity).first,
                        getSlidingRegionSize(gravity).second,
                        fromGravityToGravity(gravity))
        );

        mGestureDetector = new GestureDetector(this, new BrightnessGestureListener(this, gravity));
    }

    private void setThickness(net.rongsonho.brightnessking.setting.data.Gravity gravity, int thickness) {
        int width, height;

        switch (gravity) {
            case LEFT:
            case RIGHT:
                width = thickness;
                height = getSlidingRegionSize(gravity).second;
                break;
            default:
                width = getSlidingRegionSize(gravity).first;
                height = thickness;
        }

        mWindowManager.updateViewLayout(
                mSlidingRegion,
                getWindowLayoutParams(
                        width,
                        height,
                        fromGravityToGravity(gravity))
        );
    }

    private int fromGravityToGravity(net.rongsonho.brightnessking.setting.data.Gravity gravity) {
        int viewGravity;
        switch (gravity) {
            case RIGHT:
                viewGravity = Gravity.END;
                break;
            case LEFT:
                viewGravity = Gravity.START;
                break;
            case TOP:
                viewGravity = Gravity.TOP;
                break;
            default:
                viewGravity = Gravity.BOTTOM;
        }
        return viewGravity;
    }

    public interface OnGravityChangedListener {
        void setGravity(net.rongsonho.brightnessking.setting.data.Gravity gravity);
    }

    public interface OnThicknessChangedListener {
        void setThickness(int thickness);
    }
}
