package com.maassoft.colorcontrol.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.models.ConnectionConfig;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefsManager {
    private static final String TAG = "SharedPrefsManager";
    
    private static final String PREFS_NAME = "ColorControlPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_LAST_CONNECTED_DEVICE = "last_connected_device";
    private static final String KEY_SAVED_DEVICES = "saved_devices";
    private static final String KEY_CONNECTION_CONFIG = "connection_config";
    private static final String KEY_AUTO_CONNECT = "auto_connect";
    private static final String KEY_AUTO_WAKE = "auto_wake";
    private static final String KEY_VIBRATION_FEEDBACK = "vibration_feedback";
    private static final String KEY_SOUND_FEEDBACK = "sound_feedback";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public SharedPrefsManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    // First Launch Methods
    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }
    
    public void setFirstLaunch(boolean firstLaunch) {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, firstLaunch).apply();
    }
    
    // Last Connected Device Methods
    public TvDevice getLastConnectedDevice() {
        String deviceJson = sharedPreferences.getString(KEY_LAST_CONNECTED_DEVICE, null);
        if (deviceJson != null) {
            try {
                return gson.fromJson(deviceJson, TvDevice.class);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error parsing last connected device: " + e.getMessage());
            }
        }
        return null;
    }
    
    public void setLastConnectedDevice(TvDevice device) {
        if (device != null) {
            String deviceJson = gson.toJson(device);
            sharedPreferences.edit().putString(KEY_LAST_CONNECTED_DEVICE, deviceJson).apply();
        } else {
            sharedPreferences.edit().remove(KEY_LAST_CONNECTED_DEVICE).apply();
        }
    }
    
    // Saved Devices Methods
    public List<TvDevice> getSavedDevices() {
        String devicesJson = sharedPreferences.getString(KEY_SAVED_DEVICES, null);
        if (devicesJson != null) {
            try {
                Type listType = new TypeToken<List<TvDevice>>(){}.getType();
                List<TvDevice> devices = gson.fromJson(devicesJson, listType);
                return devices != null ? devices : new ArrayList<>();
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error parsing saved devices: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }
    
    public void saveDevice(TvDevice device) {
        if (device == null) return;
        
        List<TvDevice> devices = getSavedDevices();
        
        // Check if device already exists
        boolean exists = false;
        for (int i = 0; i < devices.size(); i++) {
            TvDevice existing = devices.get(i);
            if (existing.getIp().equals(device.getIp()) && 
                existing.getBrand().equals(device.getBrand())) {
                devices.set(i, device); // Update existing
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            devices.add(device);
        }
        
        saveDevicesList(devices);
    }
    
    public void removeDevice(TvDevice device) {
        if (device == null) return;
        
        List<TvDevice> devices = getSavedDevices();
        devices.removeIf(d -> d.getIp().equals(device.getIp()) && d.getBrand().equals(device.getBrand()));
        saveDevicesList(devices);
    }
    
    private void saveDevicesList(List<TvDevice> devices) {
        String devicesJson = gson.toJson(devices);
        sharedPreferences.edit().putString(KEY_SAVED_DEVICES, devicesJson).apply();
    }
    
    // Connection Config Methods
    public ConnectionConfig getConnectionConfig() {
        String configJson = sharedPreferences.getString(KEY_CONNECTION_CONFIG, null);
        if (configJson != null) {
            try {
                return gson.fromJson(configJson, ConnectionConfig.class);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error parsing connection config: " + e.getMessage());
            }
        }
        return new ConnectionConfig(); // Return default config
    }
    
    public void saveConnectionConfig(ConnectionConfig config) {
        if (config != null) {
            String configJson = gson.toJson(config);
            sharedPreferences.edit().putString(KEY_CONNECTION_CONFIG, configJson).apply();
        }
    }
    
    // Settings Methods
    public boolean isAutoConnectEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTO_CONNECT, true);
    }
    
    public void setAutoConnectEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_CONNECT, enabled).apply();
    }
    
    public boolean isAutoWakeEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTO_WAKE, true);
    }
    
    public void setAutoWakeEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_WAKE, enabled).apply();
    }
    
    public boolean isVibrationFeedbackEnabled() {
        return sharedPreferences.getBoolean(KEY_VIBRATION_FEEDBACK, true);
    }
    
    public void setVibrationFeedbackEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_VIBRATION_FEEDBACK, enabled).apply();
    }
    
    public boolean isSoundFeedbackEnabled() {
        return sharedPreferences.getBoolean(KEY_SOUND_FEEDBACK, false);
    }
    
    public void setSoundFeedbackEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_SOUND_FEEDBACK, enabled).apply();
    }
    
    public boolean isDarkModeEnabled() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, true);
    }
    
    public void setDarkModeEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }
    
    public boolean isKeepScreenOnEnabled() {
        return sharedPreferences.getBoolean(KEY_KEEP_SCREEN_ON, false);
    }
    
    public void setKeepScreenOnEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply();
    }
    
    // Clear all data
    public void clearAllData() {
        sharedPreferences.edit().clear().apply();
    }
    
    // Migration methods for future updates
    public void migrateFromOldVersion(int oldVersion, int newVersion) {
        // Implement migration logic if needed
        android.util.Log.d(TAG, "Migrating from version " + oldVersion + " to " + newVersion);
    }
}