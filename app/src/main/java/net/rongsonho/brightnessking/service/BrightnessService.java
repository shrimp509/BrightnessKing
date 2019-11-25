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
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;

import androidx.core.app.NotificationCompat;

import net.rongsonho.brightnessking.R;
import net.rongsonho.brightnessking.service.util.BrightnessGestureListener;

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
        Log.d(TAG, "onCreate");

        createSlidingRegion();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // clear the block
        if (mSlidingRegion != null) {
            // shrink anim
            mSlidingRegion.animate().scaleX(0f).scaleY(0f).setDuration(300)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(()-> {
                        Log.d(TAG, "onDestroy, onShrinkAnimationEnd, removeView");
                        // remove view
                        mWindowManager.removeView(mSlidingRegion);
                        super.onDestroy();
                    }).start();
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
        Pair<Integer, Integer> regionSize = getSlidingRegionSize();
        int regionWidth = regionSize.first;
        int regionHeight = regionSize.second;

        // create rect region
        mSlidingRegion = getSlidingRegion(regionWidth, regionHeight, Color.LTGRAY, 0.2f);

        // draw rect
        mWindowManager.addView(mSlidingRegion, getWindowLayoutParams(regionWidth, regionHeight, Gravity.BOTTOM));

        // show popup animate when created
        setPopUpAnimation(mSlidingRegion);

        // set sliding behavior on rect
        mGestureDetector = new GestureDetector(this, new BrightnessGestureListener(getApplicationContext()));
        mSlidingRegion.setOnTouchListener(mTouchListener);

        // set notification channel
        setNotificationChannel();
    }

    // TODO: Implement the change rect width and height part
    public void changeRectWidth(int widthChanged) {
        int originalWidth = mSlidingRegion.getMeasuredWidth();
        int originalHeight = mSlidingRegion.getMeasuredHeight();

        mWindowManager.updateViewLayout(
                mSlidingRegion,
                getWindowLayoutParams(
                        originalWidth + widthChanged,
                        originalHeight,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL)
        );
    }

    public void changeRectHeight(int heightChanged) {
        int originalWidth = mSlidingRegion.getMeasuredWidth();
        int originalHeight = mSlidingRegion.getMeasuredHeight();

        mWindowManager.updateViewLayout(
                mSlidingRegion,
                getWindowLayoutParams(
                        originalWidth,
                        originalHeight + heightChanged,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL)
        );
    }

    private WindowManager.LayoutParams getWindowLayoutParams(int width, int height, int gravity){
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
        return windowParams;
    }

    // get screen width and height
    private Pair<Integer, Integer> getScreenResolution() {
        DisplayMetrics display = getResources().getDisplayMetrics();
        int deviceHeight = display.heightPixels;
        int deviceWidth = display.widthPixels;
        return new Pair<>(deviceWidth, deviceHeight);
    }

    private Pair<Integer, Integer> getSlidingRegionSize() {
         Pair<Integer, Integer> screenSize = getScreenResolution();
         int screenWidth = screenSize.first;
         int screenHeight = screenSize.second;
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

    private void setPopUpAnimation(View view) {
        view.setScaleX(0.1f);
        view.setScaleY(0.1f);
        view.animate().scaleX(1f).scaleY(1f).setStartDelay(1500).setDuration(500).setInterpolator(new BounceInterpolator()).start();
    }
}
