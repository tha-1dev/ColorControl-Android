package com.maassoft.colorcontrol.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.maassoft.colorcontrol.controllers.TvCommandProcessor;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.network.UniversalConnectionManager;
import com.maassoft.colorcontrol.utils.SharedPrefsManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TvControlService extends Service {
    private static final String TAG = "TvControlService";
    
    private final IBinder binder = new TvControlBinder();
    private UniversalConnectionManager connectionManager;
    private TvCommandProcessor commandProcessor;
    private SharedPrefsManager prefsManager;
    private ExecutorService executorService;
    
    private TvDevice currentDevice;
    private boolean isConnected = false;
    private ServiceCallback serviceCallback;
    
    public interface ServiceCallback {
        void onConnectionStateChanged(boolean connected);
        void onCommandSent(String command, boolean success);
        void onError(String errorMessage);
    }
    
    public class TvControlBinder extends Binder {
        public TvControlService getService() {
            return TvControlService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TvControlService created");
        
        connectionManager = new UniversalConnectionManager(this);
        commandProcessor = new TvCommandProcessor();
        prefsManager = new SharedPrefsManager(this);
        executorService = Executors.newSingleThreadExecutor();
        
        setupConnectionManager();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "TvControlService started");
        
        if (intent != null) {
            String action = intent.getAction();
            if ("CONNECT_DEVICE".equals(action)) {
                TvDevice device = intent.getParcelableExtra("device");
                if (device != null) {
                    connectToDevice(device);
                }
            } else if ("SEND_COMMAND".equals(action)) {
                String command = intent.getStringExtra("command");
                if (command != null) {
                    sendCommand(command);
                }
            } else if ("DISCONNECT".equals(action)) {
                disconnect();
            } else if ("WAKE_UP".equals(action)) {
                wakeUpTv();
            }
        }
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    private void setupConnectionManager() {
        connectionManager.setConnectionStateListener(state -> {
            boolean connected = state == UniversalConnectionManager.ConnectionState.CONNECTED;
            this.isConnected = connected;
            
            if (serviceCallback != null) {
                serviceCallback.onConnectionStateChanged(connected);
            }
            
            if (connected && currentDevice != null) {
                // Save as last connected device
                prefsManager.setLastConnectedDevice(currentDevice);
            }
        });
    }
    
    public void connectToDevice(TvDevice device) {
        if (device == null || !device.isValid()) {
            notifyError("Invalid device");
            return;
        }
        
        executorService.execute(() -> {
            try {
                currentDevice = device;
                
                // First try with command processor
                boolean processorConnected = commandProcessor.connect(device.getIp(), device.getBrand());
                
                if (processorConnected) {
                    Log.i(TAG, "Connected via TvCommandProcessor to " + device.getName());
                    isConnected = true;
                    if (serviceCallback != null) {
                        serviceCallback.onConnectionStateChanged(true);
                    }
                    return;
                }
                
                // Fall back to universal connection manager
                UniversalConnectionManager.ConnectionType connectionType = 
                    UniversalConnectionManager.ConnectionType.valueOf(
                        device.getConnectionType().toUpperCase()
                    );
                
                connectionManager.connect(device, connectionType);
                
            } catch (Exception e) {
                Log.e(TAG, "Error connecting to device: " + e.getMessage());
                notifyError("Connection failed: " + e.getMessage());
            }
        });
    }
    
    public void sendCommand(String command) {
        if (!isConnected || currentDevice == null) {
            notifyError("Not connected to any device");
            return;
        }
        
        executorService.execute(() -> {
            try {
                boolean success = commandProcessor.sendCommand(command);
                
                if (!success) {
                    // Fall back to connection manager
                    success = connectionManager.sendCommand(command);
                }
                
                if (serviceCallback != null) {
                    serviceCallback.onCommandSent(command, success);
                }
                
                if (success) {
                    Log.d(TAG, "Command sent successfully: " + command);
                } else {
                    Log.w(TAG, "Failed to send command: " + command);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending command: " + e.getMessage());
                notifyError("Command failed: " + e.getMessage());
            }
        });
    }
    
    public void wakeUpTv() {
        if (currentDevice == null) {
            notifyError("No device selected");
            return;
        }
        
        executorService.execute(() -> {
            try {
                boolean success = commandProcessor.wakeUp();
                
                if (success) {
                    Log.i(TAG, "TV wake-up command sent successfully");
                } else {
                    Log.w(TAG, "TV wake-up command failed");
                    // Try alternative wake methods
                    tryAlternativeWakeMethods();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error waking up TV: " + e.getMessage());
                notifyError("Wake-up failed: " + e.getMessage());
            }
        });
    }
    
    private void tryAlternativeWakeMethods() {
        // Implement alternative wake methods here
        Log.d(TAG, "Trying alternative wake methods");
        
        // Method 1: Send multiple power commands
        for (int i = 0; i < 3; i++) {
            sendCommand("POWER");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Method 2: Use Wake-on-LAN if MAC address is available
        if (currentDevice.getMacAddress() != null) {
            // Implementation would go here
            Log.d(TAG, "Attempting Wake-on-LAN for MAC: " + currentDevice.getMacAddress());
        }
    }
    
    public void disconnect() {
        executorService.execute(() -> {
            try {
                commandProcessor.disconnect();
                connectionManager.disconnect();
                isConnected = false;
                currentDevice = null;
                
                if (serviceCallback != null) {
                    serviceCallback.onConnectionStateChanged(false);
                }
                
                Log.i(TAG, "Disconnected from device");
                
            } catch (Exception e) {
                Log.e(TAG, "Error during disconnect: " + e.getMessage());
            }
        });
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public TvDevice getCurrentDevice() {
        return currentDevice;
    }
    
    public void setServiceCallback(ServiceCallback callback) {
        this.serviceCallback = callback;
    }
    
    private void notifyError(String errorMessage) {
        if (serviceCallback != null) {
            serviceCallback.onError(errorMessage);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TvControlService destroyed");
        
        // Clean up resources
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        disconnect();
    }
    
    // Public methods for activity binding
    public void sendPowerCommand() {
        sendCommand("POWER");
    }
    
    public void sendVolumeUp() {
        sendCommand("VOLUME_UP");
    }
    
    public void sendVolumeDown() {
        sendCommand("VOLUME_DOWN");
    }
    
    public void sendMute() {
        sendCommand("MUTE");
    }
    
    public void sendChannelUp() {
        sendCommand("CHANNEL_UP");
    }
    
    public void sendChannelDown() {
        sendCommand("CHANNEL_DOWN");
    }
    
    public void sendHome() {
        sendCommand("HOME");
    }
    
    public void sendBack() {
        sendCommand("BACK");
    }
}