package com.maassoft.colorcontrol.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.network.TvDiscovery;
import com.maassoft.colorcontrol.utils.SharedPrefsManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundDiscoveryService extends Service {
    private static final String TAG = "BackgroundDiscoveryService";
    
    private static final long DISCOVERY_INTERVAL = 30000; // 30 seconds
    private static final long INITIAL_DELAY = 5000; // 5 seconds
    
    private ExecutorService executorService;
    private Handler discoveryHandler;
    private TvDiscovery tvDiscovery;
    private SharedPrefsManager prefsManager;
    
    private List<TvDevice> lastDiscoveredDevices = new ArrayList<>();
    private boolean isDiscoveryRunning = false;
    private DiscoveryCallback discoveryCallback;
    
    public interface DiscoveryCallback {
        void onDevicesDiscovered(List<TvDevice> devices);
        void onDiscoveryStarted();
        void onDiscoveryStopped();
        void onDiscoveryError(String error);
    }
    
    private Runnable discoveryRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isDiscoveryRunning) {
                return;
            }
            
            performDiscovery();
            
            // Schedule next discovery
            if (isDiscoveryRunning) {
                discoveryHandler.postDelayed(this, DISCOVERY_INTERVAL);
            }
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BackgroundDiscoveryService created");
        
        executorService = Executors.newSingleThreadExecutor();
        discoveryHandler = new Handler();
        tvDiscovery = new TvDiscovery(this);
        prefsManager = new SharedPrefsManager(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BackgroundDiscoveryService started");
        
        if (intent != null) {
            String action = intent.getAction();
            if ("START_DISCOVERY".equals(action)) {
                startBackgroundDiscovery();
            } else if ("STOP_DISCOVERY".equals(action)) {
                stopBackgroundDiscovery();
            } else if ("DISCOVER_NOW".equals(action)) {
                discoverNow();
            }
        } else {
            // Auto-start if enabled in settings
            if (prefsManager.isAutoConnectEnabled()) {
                startBackgroundDiscovery();
            }
        }
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // This service doesn't support binding
    }
    
    public void startBackgroundDiscovery() {
        if (isDiscoveryRunning) {
            Log.d(TAG, "Discovery already running");
            return;
        }
        
        Log.i(TAG, "Starting background discovery service");
        isDiscoveryRunning = true;
        
        if (discoveryCallback != null) {
            discoveryCallback.onDiscoveryStarted();
        }
        
        // Start discovery with initial delay
        discoveryHandler.postDelayed(discoveryRunnable, INITIAL_DELAY);
    }
    
    public void stopBackgroundDiscovery() {
        Log.i(TAG, "Stopping background discovery service");
        isDiscoveryRunning = false;
        discoveryHandler.removeCallbacks(discoveryRunnable);
        
        if (discoveryCallback != null) {
            discoveryCallback.onDiscoveryStopped();
        }
    }
    
    public void discoverNow() {
        if (!isDiscoveryRunning) {
            Log.d(TAG, "Discovery not running, starting now");
            startBackgroundDiscovery();
            return;
        }
        
        // Remove any pending discoveries and run immediately
        discoveryHandler.removeCallbacks(discoveryRunnable);
        discoveryHandler.post(discoveryRunnable);
    }
    
    private void performDiscovery() {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Performing background device discovery");
                
                List<TvDevice> discoveredDevices = tvDiscovery.discoverTvs();
                
                // Check if device list has changed
                if (hasDeviceListChanged(discoveredDevices)) {
                    lastDiscoveredDevices = new ArrayList<>(discoveredDevices);
                    
                    // Notify callback
                    if (discoveryCallback != null) {
                        discoveryCallback.onDevicesDiscovered(discoveredDevices);
                    }
                    
                    // Auto-connect to last device if enabled
                    if (prefsManager.isAutoConnectEnabled() && !discoveredDevices.isEmpty()) {
                        attemptAutoConnect(discoveredDevices);
                    }
                    
                    Log.i(TAG, "Discovered " + discoveredDevices.size() + " devices");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error during background discovery: " + e.getMessage());
                if (discoveryCallback != null) {
                    discoveryCallback.onDiscoveryError("Discovery failed: " + e.getMessage());
                }
            }
        });
    }
    
    private boolean hasDeviceListChanged(List<TvDevice> newDevices) {
        if (lastDiscoveredDevices.size() != newDevices.size()) {
            return true;
        }
        
        for (TvDevice newDevice : newDevices) {
            boolean found = false;
            for (TvDevice oldDevice : lastDiscoveredDevices) {
                if (newDevice.equals(oldDevice)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }
        
        return false;
    }
    
    private void attemptAutoConnect(List<TvDevice> discoveredDevices) {
        TvDevice lastDevice = prefsManager.getLastConnectedDevice();
        if (lastDevice == null) {
            return;
        }
        
        // Check if last connected device is in the discovered list
        for (TvDevice device : discoveredDevices) {
            if (device.equals(lastDevice)) {
                Log.i(TAG, "Auto-connecting to last device: " + device.getName());
                
                // Send broadcast to connect to this device
                Intent connectIntent = new Intent("AUTO_CONNECT_DEVICE");
                connectIntent.putExtra("device", device);
                sendBroadcast(connectIntent);
                
                break;
            }
        }
    }
    
    public void setDiscoveryCallback(DiscoveryCallback callback) {
        this.discoveryCallback = callback;
    }
    
    public boolean isDiscoveryRunning() {
        return isDiscoveryRunning;
    }
    
    public List<TvDevice> getLastDiscoveredDevices() {
        return new ArrayList<>(lastDiscoveredDevices);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BackgroundDiscoveryService destroyed");
        
        stopBackgroundDiscovery();
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        if (discoveryHandler != null) {
            discoveryHandler.removeCallbacks(discoveryRunnable);
        }
    }
    
    // Utility methods for controlling discovery behavior
    public void setDiscoveryInterval(long intervalMs) {
        // This would require restarting the discovery with new interval
        Log.d(TAG, "Discovery interval set to: " + intervalMs + "ms");
    }
    
    public void enableAutoConnect(boolean enable) {
        if (enable && !isDiscoveryRunning) {
            startBackgroundDiscovery();
        } else if (!enable && isDiscoveryRunning) {
            stopBackgroundDiscovery();
        }
    }
}