package net.rongsonho.brightnessking;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.crashlytics.android.Crashlytics;

public class BrightnessService extends Service {
    private static final String TAG = "BrightnessService";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private WindowManager mWindowManager;
    private GestureDetector mGestureDetector;

    private int deviceHeight, deviceWidth;
    private int rectWidth, rectHeight;
    private float rectAlpha = 0.2f;

    private Toast mLastToast;
    private LinearLayout mLinearLayout;
    private StorageHelper mDatabase;

    private boolean toastOn = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        DisplayMetrics display = getResources().getDisplayMetrics();
        deviceHeight = display.heightPixels;
        deviceWidth = display.widthPixels;

        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setBackgroundColor(Color.LTGRAY);
        mLinearLayout.setAlpha(rectAlpha);
        LinearLayout.LayoutParams layoutParams;

        // Right side
//        rectWidth = deviceWidth / 20;
//        rectHeight = (int) (deviceHeight / 1.5);

        // Bottom side
        rectWidth = deviceWidth;
        rectHeight = (int) (deviceHeight / 30);

        if (deviceHeight != 0 || deviceWidth != 0) {
            layoutParams = new LinearLayout.LayoutParams(rectWidth, rectHeight);
        } else {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400);
        }
        mLinearLayout.setLayoutParams(layoutParams);

        final WindowManager.LayoutParams windowParams;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowParams = new WindowManager.LayoutParams(rectWidth, rectHeight, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        } else {
            windowParams = new WindowManager.LayoutParams(rectWidth, rectHeight, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        }

        // Right side
//        windowParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        // Bottom side
        windowParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        windowParams.x = 0;
        windowParams.y = 0;

        mWindowManager.addView(mLinearLayout, windowParams);

        mGestureDetector = new GestureDetector(this, new MyGestureListener());

        mLinearLayout.setOnTouchListener(mTouchListener);


        // set storage helper
        mDatabase = new StorageHelper(getApplicationContext());
        mDatabase.setIsActivate(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();
            startForeground(1, notification);
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (mLinearLayout != null){
            mWindowManager.removeView(mLinearLayout);
        }

        mDatabase.setIsActivate(false);
        showToast("service is destroyed, on purpose? " + mDatabase.getOnPurpose());

        if (mDatabase.getOnPurpose()) {
            sendBroadcast(
                    new Intent(this, RestartReceiver.class).setAction("Restart")
            );
            showToast("service try to restart itself");
        }

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    public void showToast(String msg){
        if (!toastOn) {
            return;
        }
        if (mLastToast != null){
            mLastToast.cancel();
        }

        Log.d(TAG, msg);
        mLastToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        mLastToast.show();
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        private float lastX = 0, lastY = 0, deltaX = 0, deltaY = 0;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Right side
//            deltaY = distanceY - lastY;
//
//            if (deltaY > 0){
//                Log.d(TAG, "onScroll, scroll up: " + deltaY);
//            }else{
//                Log.d(TAG, "onScroll, scroll down: " + deltaY);
//            }
//
//            setBrightness(deltaY);

            // Bottom side
            deltaX = distanceX - lastX;

            if (deltaX < 0){
                Log.d(TAG, "onScroll, scroll right: " + deltaX);
            }else{
                Log.d(TAG, "onScroll, scroll left: " + deltaX);
            }

            setBrightness(-deltaX);

            return true;
        }

        private void setBrightness(float brightnessDelta){
            ContentResolver contentResolver = getApplicationContext().getContentResolver();
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

        private void setBrightness(int brightness){
            ContentResolver contentResolver = getApplicationContext().getContentResolver();
            if (brightness > 0 && brightness <= 255){
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            }
        }
    }

    // TODO: Implement the change rect width and height part
    public void changeRectWidth(int widthChanged){
        int originalWidth = mLinearLayout.getMeasuredWidth();
        int originalHeight = mLinearLayout.getMeasuredHeight();

        mWindowManager.updateViewLayout(
                mLinearLayout,
                getWindowLayoutParams(
                        originalWidth + widthChanged,
                        originalHeight,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL)
        );
    }

    public void changeRectHeight(int heightChanged){
        int originalWidth = mLinearLayout.getMeasuredWidth();
        int originalHeight = mLinearLayout.getMeasuredHeight();

        mWindowManager.updateViewLayout(
                mLinearLayout,
                getWindowLayoutParams(
                        originalWidth,
                        originalHeight + heightChanged,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL)
        );
    }

    private WindowManager.LayoutParams getWindowLayoutParams(int width, int height, int gravity){
        final WindowManager.LayoutParams windowParams;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowParams = new WindowManager.LayoutParams(width, height, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        } else {
            windowParams = new WindowManager.LayoutParams(width, height, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        }
        windowParams.gravity = gravity;
        windowParams.x = 0;
        windowParams.y = 0;
        return windowParams;
    }

    public class LocalBinder extends Binder {
        BrightnessService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BrightnessService.this;
        }
    }

}
