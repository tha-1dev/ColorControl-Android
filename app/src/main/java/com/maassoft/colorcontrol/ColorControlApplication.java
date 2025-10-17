package com.maassoft.colorcontrol;

import android.app.Application;
import android.util.Log;

public class ColorControlApplication extends Application {
    private static final String TAG = "ColorControlApp";
    private static ColorControlApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "ColorControl Application Started");
        
        // Initialize global components
        initializeApp();
    }

    private void initializeApp() {
        // Initialize any global components here
        Log.d(TAG, "Initializing application components");
    }

    public static ColorControlApplication getInstance() {
        return instance;
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "Application terminating");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        Log.w(TAG, "Low memory warning");
        super.onLowMemory();
    }
}