package com.maassoft.colorcontrol.network;

import android.content.Context;
import android.util.Log;
import com.maassoft.colorcontrol.bluetooth.AdvancedBluetoothConnection;
import com.maassoft.colorcontrol.controllers.EnhancedLgController;
import com.maassoft.colorcontrol.controllers.EnhancedSamsungController;
import com.maassoft.colorcontrol.controllers.SonyController;
import com.maassoft.colorcontrol.controllers.TvCommandProcessor;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.usb.UsbOtgConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UniversalConnectionManager {
    private static final String TAG = "UniversalConnectionManager";
    
    public enum ConnectionType {
        WIFI, BLUETOOTH, USB, AUTO
    }
    
    public enum ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }
    
    private Context context;
    private ConnectionType currentType;
    private ConnectionState currentState;
    private ConnectionStateListener stateListener;
    
    // Connection components
    private TvCommandProcessor commandProcessor;
    private AdvancedBluetoothConnection bluetoothConnection;
    private UsbOtgConnection usbConnection;
    
    private ExecutorService connectionExecutor;
    private TvDevice currentDevice;
    
    public UniversalConnectionManager(Context context) {
        this.context = context;
        this.connectionExecutor = Executors.newSingleThreadExecutor();
        this.currentState = ConnectionState.DISCONNECTED;
        
        initializeConnections();
    }
    
    private void initializeConnections() {
        commandProcessor = new TvCommandProcessor();
        bluetoothConnection = new AdvancedBluetoothConnection(context);
        usbConnection = new UsbOtgConnection(context);
        
        setupBluetoothCallbacks();
        setupUsbCallbacks();
    }
    
    private void setupBluetoothCallbacks() {
        bluetoothConnection.setConnectionListener(new AdvancedBluetoothConnection.BluetoothConnectionListener() {
            @Override
            public void onBluetoothConnected(android.bluetooth.BluetoothDevice device) {
                Log.i(TAG, "Bluetooth connected: " + device.getName());
                setState(ConnectionState.CONNECTED);
            }
            
            @Override
            public void onBluetoothDisconnected() {
                Log.i(TAG, "Bluetooth disconnected");
                setState(ConnectionState.DISCONNECTED);
            }
            
            @Override
            public void onBluetoothConnectionFailed(String error) {
                Log.e(TAG, "Bluetooth connection failed: " + error);
                setState(ConnectionState.ERROR);
            }
            
            @Override
            public void onBluetoothDataReceived(byte[] data) {
                Log.d(TAG, "Bluetooth data received: " + new String(data));
            }
            
            @Override
            public void onBluetoothDeviceFound(android.bluetooth.BluetoothDevice device) {
                Log.d(TAG, "Bluetooth device found: " + device.getName());
            }
        });
    }
    
    private void setupUsbCallbacks() {
        // USB callbacks would be implemented here
    }
    
    public void connect(TvDevice device, ConnectionType preferredType) {
        if (currentState == ConnectionState.CONNECTING) {
            Log.w(TAG, "Already connecting, please wait");
            return;
        }
        
        if (device == null || !device.isValid()) {
            Log.e(TAG, "Invalid device provided");
            setState(ConnectionState.ERROR);
            return;
        }
        
        currentDevice = device;
        setState(ConnectionState.CONNECTING);
        
        connectionExecutor.execute(() -> {
            boolean success = false;
            
            switch (preferredType) {
                case WIFI:
                    success = connectViaWifi(device);
                    break;
                case BLUETOOTH:
                    success = connectViaBluetooth(device);
                    break;
                case USB:
                    success = connectViaUsb(device);
                    break;
                case AUTO:
                    success = autoConnect(device);
                    break;
            }
            
            if (success) {
                currentType = preferredType;
                setState(ConnectionState.CONNECTED);
                Log.i(TAG, "Successfully connected to " + device.getName() + " via " + preferredType);
            } else {
                setState(ConnectionState.ERROR);
                Log.e(TAG, "Failed to connect to " + device.getName());
            }
        });
    }
    
    private boolean connectViaWifi(TvDevice device) {
        Log.d(TAG, "Attempting WiFi connection to " + device.getIp());
        
        try {
            // Use command processor for WiFi connection
            return commandProcessor.connect(device.getIp(), device.getBrand());
        } catch (Exception e) {
            Log.e(TAG, "WiFi connection failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean connectViaBluetooth(TvDevice device) {
        Log.d(TAG, "Attempting Bluetooth connection to " + device.getName());
        
        try {
            return bluetoothConnection.connectToTvBluetooth(device);
        } catch (Exception e) {
            Log.e(TAG, "Bluetooth connection failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean connectViaUsb(TvDevice device) {
        Log.d(TAG, "Attempting USB connection");
        
        try {
            return usbConnection.connectViaUsb(device);
        } catch (Exception e) {
            Log.e(TAG, "USB connection failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean autoConnect(TvDevice device) {
        Log.d(TAG, "Starting auto-connection for " + device.getName());
        
        ConnectionType[] priorityOrder = {
            ConnectionType.WIFI,
            ConnectionType.BLUETOOTH, 
            ConnectionType.USB
        };
        
        for (ConnectionType type : priorityOrder) {
            Log.d(TAG, "Trying connection via: " + type);
            
            boolean success = false;
            switch (type) {
                case WIFI:
                    success = connectViaWifi(device);
                    break;
                case BLUETOOTH:
                    success = connectViaBluetooth(device);
                    break;
                case USB:
                    success = connectViaUsb(device);
                    break;
            }
            
            if (success) {
                currentType = type;
                Log.i(TAG, "Auto-connected via " + type);
                return true;
            }
            
            // Small delay between connection attempts
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        Log.e(TAG, "All auto-connection attempts failed");
        return false;
    }
    
    public boolean sendCommand(String command) {
        if (currentState != ConnectionState.CONNECTED) {
            Log.w(TAG, "Not connected, cannot send command");
            return false;
        }
        
        if (currentDevice == null) {
            Log.e(TAG, "No device connected");
            return false;
        }
        
        Log.d(TAG, "Sending command: " + command + " via " + currentType);
        
        switch (currentType) {
            case WIFI:
                return commandProcessor.sendCommand(command);
            case BLUETOOTH:
                return bluetoothConnection.sendCommand(command);
            case USB:
                return usbConnection.sendCommand(command);
            default:
                Log.e(TAG, "Unknown connection type: " + currentType);
                return false;
        }
    }
    
    public boolean wakeUpTv() {
        if (currentDevice == null) {
            Log.e(TAG, "No device to wake up");
            return false;
        }
        
        Log.i(TAG, "Attempting to wake up TV: " + currentDevice.getName());
        
        // Try command processor first
        if (commandProcessor.wakeUp()) {
            return true;
        }
        
        // Fallback to connection-specific wake methods
        switch (currentType) {
            case WIFI:
                // Additional WiFi wake methods
                return tryNetworkWake();
            case BLUETOOTH:
                // Bluetooth wake methods
                return bluetoothConnection.sendCommand("POWER");
            case USB:
                // USB wake methods
                return usbConnection.sendCommand("POWER");
            default:
                return false;
        }
    }
    
    private boolean tryNetworkWake() {
        if (currentDevice == null || currentDevice.getMacAddress() == null) {
            return false;
        }
        
        // Try Wake-on-LAN
        return WakeOnLan.sendWakeOnLan(currentDevice.getMacAddress(), currentDevice.getIp());
    }
    
    public void disconnect() {
        Log.i(TAG, "Disconnecting from current device");
        
        connectionExecutor.execute(() -> {
            switch (currentType) {
                case WIFI:
                    commandProcessor.disconnect();
                    break;
                case BLUETOOTH:
                    bluetoothConnection.disconnect();
                    break;
                case USB:
                    usbConnection.disconnect();
                    break;
            }
            
            currentDevice = null;
            currentType = null;
            setState(ConnectionState.DISCONNECTED);
        });
    }
    
    public void reconnect() {
        if (currentDevice != null && currentType != null) {
            Log.i(TAG, "Attempting to reconnect to " + currentDevice.getName());
            connect(currentDevice, currentType);
        }
    }
    
    private void setState(ConnectionState state) {
        this.currentState = state;
        if (stateListener != null) {
            stateListener.onStateChanged(state);
        }
    }
    
    public void setConnectionStateListener(ConnectionStateListener listener) {
        this.stateListener = listener;
    }
    
    public ConnectionState getCurrentState() {
        return currentState;
    }
    
    public ConnectionType getCurrentType() {
        return currentType;
    }
    
    public TvDevice getCurrentDevice() {
        return currentDevice;
    }
    
    public boolean isConnected() {
        return currentState == ConnectionState.CONNECTED;
    }
    
    public boolean isConnecting() {
        return currentState == ConnectionState.CONNECTING;
    }
    
    public void cleanup() {
        disconnect();
        
        if (connectionExecutor != null && !connectionExecutor.isShutdown()) {
            connectionExecutor.shutdown();
        }
        
        if (bluetoothConnection != null) {
            bluetoothConnection.cleanup();
        }
        
        if (usbConnection != null) {
            usbConnection.cleanup();
        }
        
        if (commandProcessor != null) {
            commandProcessor.disconnect();
        }
    }
    
    public interface ConnectionStateListener {
        void onStateChanged(ConnectionState state);
    }
}